package ch.eth.jcd.badgers.vfs.test.core.interfaces;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.mock.MockedVFSDiskManagerImpl;

@RunWith(Parameterized.class)
public class VFSEntryTest {

	private static VFSDiskManager diskManager;

	public VFSEntryTest(VFSDiskManager manager) {
		this.diskManager = manager;
	}

	private static DiskConfiguration getMockedConfig(String rootFolderName) {
		DiskConfiguration config = new DiskConfiguration();
		config.setHostFilePath(getRootDir(rootFolderName));
		return config;

	}

	private static String getRootDir(String rootFolderName) {
		String tempDir = System.getProperty("java.io.tmpdir");
		String fileName;
		if (tempDir.endsWith("/") || tempDir.endsWith("\\")) {
			fileName = tempDir + rootFolderName;
		} else {
			fileName = tempDir + File.separatorChar + rootFolderName;
		}
		return fileName;

	}

	@AfterClass
	public static void afterClass() throws VFSException {

		diskManager.dispose();

	}

	@Before
	public void beforeTest() throws VFSException {
		Class<? extends VFSDiskManager> class1;
		try {
			class1 = (Class<? extends VFSDiskManager>) Class.forName(diskManager.getClass().getName());
			Method methodOpen = class1.getMethod("open", DiskConfiguration.class);
			diskManager = (VFSDiskManager) methodOpen.invoke(null, diskManager.getDiskConfiguration());
			Assert.assertTrue("Expected File to exist", new File(diskManager.getDiskConfiguration().getHostFilePath()).exists());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

	}

	@After
	public void afterTest() throws VFSException {
		diskManager.close();
	}

	@Parameters
	public static Collection<Object[]> getParameters() throws VFSException {
		return Arrays.asList(new Object[][] { { MockedVFSDiskManagerImpl.create(getMockedConfig("MockedRoot")) },
				{ VFSDiskManagerImpl.create(getMockedConfig("BadgersDisk.bfs")) } });
	}

	@Test
	public void testIsDirectory() throws VFSException {
		String isDirectory = "IsDirectory";
		String isNotADirectory = "IsNotADirectory.txt";

		VFSEntry rootEntry = diskManager.getRoot();

		VFSPath isDirectoryPath = rootEntry.getNewChildPath(isDirectory);
		Assert.assertFalse("Expected directory not exists", isDirectoryPath.exists());
		VFSEntry directory = isDirectoryPath.createDirectory();
		Assert.assertTrue("Expected directory exists", isDirectoryPath.exists());
		Assert.assertTrue("Expected isDirectory is true", directory.isDirectory());

		VFSPath aFileNotDirectory = directory.getNewChildPath(isNotADirectory);
		Assert.assertFalse("Expected file not exists", aFileNotDirectory.exists());
		VFSEntry entry = aFileNotDirectory.createFile();
		Assert.assertTrue("Expected file exists", aFileNotDirectory.exists());
	}

	@Test
	public void testGetOutputStreamTest() throws VFSException {
		VFSEntry rootEntry = diskManager.getRoot();
		VFSPath newFile = rootEntry.getNewChildPath("newOutputStreamTest.txt");
		Assert.assertFalse("Expected file not exists", newFile.exists());
		VFSEntry entry = newFile.createFile();
		Assert.assertTrue("Expected file exists", newFile.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write("newOutputStreamTest String");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertTrue("Expected file exists", newFile.exists());
	}

	@Test
	public void testGetInputStreamTest() throws VFSException {
		String fileName = "newInputStreamTest.txt";
		String fileContent = "newInputStreamTest String";
		VFSEntry rootEntry = diskManager.getRoot();
		VFSPath newFile = rootEntry.getNewChildPath(fileName);
		Assert.assertFalse("Expected file not exists", newFile.exists());
		VFSEntry entry = newFile.createFile();
		Assert.assertTrue("Expected file exists", newFile.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write(fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertTrue("Expected file exists", newFile.exists());
		String readed = null;
		try (InputStream in = entry.getInputStream(); InputStreamReader reader = new InputStreamReader(in); BufferedReader br = new BufferedReader(reader)) {
			readed = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertEquals("Expected readed content: " + fileContent, fileContent, readed);
	}

	@Test
	public void testRenameTo() throws VFSException {
		String fileNameBefore = "beforeRename.txt";
		String fileNameAfter = "afterRename.txt";
		String fileContent = "Test String";
		VFSEntry rootEntry = diskManager.getRoot();
		VFSPath newFile = rootEntry.getNewChildPath(fileNameBefore);
		Assert.assertFalse("Expected file not exists", newFile.exists());
		VFSEntry entry = newFile.createFile();
		Assert.assertTrue("Expected file exists", newFile.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write(fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertTrue("Expected file exists", newFile.exists());

		entry.renameTo(fileNameAfter);

		Assert.assertFalse("Expected file not exists", newFile.exists());
		Assert.assertTrue("Expected file not exists", entry.getPath().exists());

		String readed = null;
		try (InputStream in = entry.getInputStream(); InputStreamReader reader = new InputStreamReader(in); BufferedReader br = new BufferedReader(reader)) {
			readed = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertEquals("Expected readed content: " + fileContent, fileContent, readed);

	}

	@Test
	public void testCopyTo() throws VFSException {
		String copyFromFolder = "CopyFrom";
		String copyToFolder = "CopyTo";
		String copyFolder = "Copy";
		String copyFile = "copy.txt";
		String fileContent = "copy file";

		VFSEntry rootEntry = diskManager.getRoot();
		VFSPath copyFromPath = rootEntry.getNewChildPath(copyFromFolder);
		Assert.assertFalse("Expected direcotry not exists", copyFromPath.exists());
		VFSEntry copyFromEntry = copyFromPath.createDirectory();
		Assert.assertTrue("Expected direcotry exists", copyFromPath.exists());

		VFSPath copyToPath = rootEntry.getNewChildPath(copyToFolder);
		Assert.assertFalse("Expected direcotry not exists", copyToPath.exists());

		VFSPath copyPath = copyFromEntry.getNewChildPath(copyFolder);
		Assert.assertFalse("Expected direcotry not exists", copyPath.exists());
		VFSEntry copyEntry = copyPath.createDirectory();
		Assert.assertTrue("Expected direcotry exists", copyPath.exists());

		VFSPath newFile = copyEntry.getNewChildPath(copyFile);
		VFSEntry entry = newFile.createFile();
		Assert.assertTrue("Expected file exists", newFile.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write(fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertTrue("Expected file exists", newFile.exists());

		copyFromEntry.copyTo(copyToPath);
		Assert.assertTrue("Expected directory exists", copyToPath.exists());
		Assert.assertTrue("Expected directory exists", copyToPath.getVFSEntry().getNewChildPath(copyFolder).exists());
		Assert.assertTrue("Expected file exists", copyToPath.getVFSEntry().getNewChildPath(copyFolder).getVFSEntry().getNewChildPath(copyFile).exists());
		Assert.assertTrue("Expected directory exists", copyFromEntry.getNewChildPath(copyFolder).exists());
		Assert.assertTrue("Expected file exists", copyFromPath.getVFSEntry().getNewChildPath(copyFolder).getVFSEntry().getNewChildPath(copyFile).exists());
		entry = copyToPath.getVFSEntry().getNewChildPath(copyFolder).getVFSEntry().getNewChildPath(copyFile).getVFSEntry();
		String readed = null;
		try (InputStream in = entry.getInputStream(); InputStreamReader reader = new InputStreamReader(in); BufferedReader br = new BufferedReader(reader)) {
			readed = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertEquals("Expected readed content: " + fileContent, fileContent, readed);
	}

	@Test
	public void testMoveTo() throws VFSException {
		String moveFromFolder = "MoveFrom";
		String moveToFolder = "MoveTo";
		String moveFolder = "Move";
		String moveFile = "move.txt";
		String fileContent = "move file";

		VFSEntry rootEntry = diskManager.getRoot();
		VFSPath copyFromPath = rootEntry.getNewChildPath(moveFromFolder);
		Assert.assertFalse("Expected direcotry not exists", copyFromPath.exists());
		VFSEntry copyFromEntry = copyFromPath.createDirectory();
		Assert.assertTrue("Expected direcotry exists", copyFromPath.exists());

		VFSPath copyToPath = rootEntry.getNewChildPath(moveToFolder);
		Assert.assertFalse("Expected direcotry not exists", copyToPath.exists());

		VFSPath copyPath = copyFromEntry.getNewChildPath(moveFolder);
		Assert.assertFalse("Expected direcotry not exists", copyPath.exists());
		VFSEntry copyEntry = copyPath.createDirectory();
		Assert.assertTrue("Expected direcotry exists", copyPath.exists());

		VFSPath newFile = copyEntry.getNewChildPath(moveFile);
		VFSEntry entry = newFile.createFile();
		Assert.assertTrue("Expected file exists", newFile.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write(fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertTrue("Expected file exists", newFile.exists());

		copyFromEntry.copyTo(copyToPath);
		Assert.assertTrue("Expected directory exists", copyToPath.exists());
		Assert.assertTrue("Expected directory exists", copyToPath.getVFSEntry().getNewChildPath(moveFolder).exists());
		Assert.assertTrue("Expected file exists", copyToPath.getVFSEntry().getNewChildPath(moveFolder).getVFSEntry().getNewChildPath(moveFile).exists());
		Assert.assertTrue("Expected directory exists", copyFromEntry.getNewChildPath(moveFolder).exists());
		Assert.assertTrue("Expected file exists", copyFromPath.getVFSEntry().getNewChildPath(moveFolder).getVFSEntry().getNewChildPath(moveFile).exists());
		entry = copyToPath.getVFSEntry().getNewChildPath(moveFolder).getVFSEntry().getNewChildPath(moveFile).getVFSEntry();
		String readed = null;
		try (InputStream in = entry.getInputStream(); InputStreamReader reader = new InputStreamReader(in); BufferedReader br = new BufferedReader(reader)) {
			readed = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertEquals("Expected readed content: " + fileContent, fileContent, readed);
	}

	@Test
	public void testGetChildren() throws VFSException {
		diskManager.dispose();
		Class<? extends VFSDiskManager> class1;
		try {
			class1 = (Class<? extends VFSDiskManager>) Class.forName(diskManager.getClass().getName());
			Method method = class1.getMethod("create", DiskConfiguration.class);
			Object o = method.invoke(null, diskManager.getDiskConfiguration());
			Method methodOpen = class1.getMethod("open", DiskConfiguration.class);
			diskManager = (VFSDiskManager) method.invoke(null, diskManager.getDiskConfiguration());
			Assert.assertTrue("Expected File to exist", new File(diskManager.getDiskConfiguration().getHostFilePath()).exists());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		String dir1 = "Dir1";
		String dir2 = "Dir2";
		String file1 = "File1.txt";

		VFSEntry rootEntry = diskManager.getRoot();
		VFSPath dir1Path = rootEntry.getNewChildPath(dir1);
		Assert.assertFalse("Expected directory not exists", dir1Path.exists());
		dir1Path.createDirectory();
		Assert.assertTrue("Expected directory exists", dir1Path.exists());

		VFSPath dir2Path = rootEntry.getNewChildPath(dir2);
		Assert.assertFalse("Expected directory not exists", dir2Path.exists());
		dir2Path.createDirectory();
		Assert.assertTrue("Expected directory exists", dir2Path.exists());

		VFSPath file1Path = rootEntry.getNewChildPath(file1);
		Assert.assertFalse("Expected file not exists", file1Path.exists());
		VFSEntry entry = file1Path.createFile();
		Assert.assertTrue("Expected file exists", file1Path.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write("Test\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<VFSEntry> childs = diskManager.getRoot().getChildren();
		Assert.assertTrue(childs.size() == 3);
		for (VFSEntry child : childs) {
			System.out.println(child);
		}

	}

	@Test
	public void testDelete() throws VFSException {
		String delDir = "delDir";
		String delFile = "delFile.txt";

		VFSEntry rootEntry = diskManager.getRoot();

		VFSPath delDirectoryPath = rootEntry.getNewChildPath(delDir);
		Assert.assertFalse("Expected directory not exists", delDirectoryPath.exists());
		VFSEntry delDirectoryEntry = delDirectoryPath.createDirectory();
		Assert.assertTrue("Expected directory exists", delDirectoryPath.exists());
		Assert.assertTrue("Expected isDirectory is true", delDirectoryEntry.isDirectory());

		VFSPath delFilePath = delDirectoryEntry.getNewChildPath(delFile);
		Assert.assertFalse("Expected file not exists", delFilePath.exists());
		VFSEntry delFileEntry = delFilePath.createFile();
		Assert.assertTrue("Expected file exists", delFilePath.exists());
		try {
			delDirectoryEntry.delete();
		} catch (Exception e) {
			Assert.assertNotNull("Expected DirectoryNotEmptyException", e);
		}

		delFileEntry.delete();
		Assert.assertFalse("Expected file not exists", delFilePath.exists());
		delDirectoryEntry.delete();
		Assert.assertFalse("Expected directory not exists", delDirectoryPath.exists());
	}

	@Test
	public void testGetParent() throws VFSException {
		String testGetParent = "testGetParent";
		VFSEntry rootEntry = diskManager.getRoot();

		VFSPath testGetParentPath = rootEntry.getNewChildPath(testGetParent);
		Assert.assertFalse("Expected directory not exists", testGetParentPath.exists());
		VFSEntry testGetParentEntry = testGetParentPath.createDirectory();
		Assert.assertTrue("Expected directory exists", testGetParentPath.exists());
		Assert.assertTrue("Expected isDirectory is true", testGetParentEntry.isDirectory());
		Assert.assertEquals(rootEntry, testGetParentEntry.getParent());

	}

}
