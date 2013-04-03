package ch.eth.jcd.badgers.vfs.test.core.interfaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public abstract class IVFSEntryTest {

	public abstract VFSDiskManager getVFSDiskManager() throws VFSException;

	public abstract void setVFSDiskManager(VFSDiskManager manager) throws VFSException;

	@Before
	public void beforeTest() throws VFSException {
		Class<? extends VFSDiskManager> class1;
		try {
			class1 = (Class<? extends VFSDiskManager>) Class.forName(getVFSDiskManager().getClass().getName());
			Method methodOpen = class1.getMethod("open", DiskConfiguration.class);
			setVFSDiskManager((VFSDiskManager) methodOpen.invoke(null, getVFSDiskManager().getDiskConfiguration()));
			assertTrue("Expected File to exist", new File(getVFSDiskManager().getDiskConfiguration().getHostFilePath()).exists());
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
		getVFSDiskManager().close();
	}

	@Test
	public void testIsDirectory() throws VFSException {
		String isDirectory = "IsDirectory";
		String isNotADirectory = "IsNotADirectory.txt";

		VFSEntry rootEntry = getVFSDiskManager().getRoot();

		VFSPath isDirectoryPath = rootEntry.getChildPath(isDirectory);
		assertFalse("Expected directory not exists", isDirectoryPath.exists());
		VFSEntry directory = isDirectoryPath.createDirectory();
		assertTrue("Expected directory exists", isDirectoryPath.exists());
		assertTrue("Expected isDirectory is true", directory.isDirectory());

		VFSPath aFileNotDirectory = directory.getChildPath(isNotADirectory);
		assertFalse("Expected file not exists", aFileNotDirectory.exists());
		aFileNotDirectory.createFile();
		assertTrue("Expected file exists", aFileNotDirectory.exists());
	}

	@Test
	public void testGetOutputStreamTest() throws VFSException {
		VFSEntry rootEntry = getVFSDiskManager().getRoot();
		VFSPath newFile = rootEntry.getChildPath("newOutputStreamTest.txt");
		assertFalse("Expected file not exists", newFile.exists());
		VFSEntry entry = newFile.createFile();
		assertTrue("Expected file exists", newFile.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write("newOutputStreamTest String");
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue("Expected file exists", newFile.exists());
	}

	@Test
	public void testGetInputStreamTest() throws VFSException {
		String fileName = "newInputStreamTest.txt";
		String fileContent = "newInputStreamTest String";
		VFSEntry rootEntry = getVFSDiskManager().getRoot();
		VFSPath newFile = rootEntry.getChildPath(fileName);
		assertFalse("Expected file not exists", newFile.exists());
		VFSEntry entry = newFile.createFile();
		assertTrue("Expected file exists", newFile.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write(fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue("Expected file exists", newFile.exists());
		String readed = null;
		try (InputStream in = entry.getInputStream(); InputStreamReader reader = new InputStreamReader(in); BufferedReader br = new BufferedReader(reader)) {
			readed = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals("Expected readed content: " + fileContent, fileContent, readed);
	}

	@Test
	public void testRenameTo() throws VFSException {
		String fileNameBefore = "beforeRename.txt";
		String fileNameAfter = "afterRename.txt";
		String fileContent = "Test String";
		VFSEntry rootEntry = getVFSDiskManager().getRoot();
		VFSPath newFile = rootEntry.getChildPath(fileNameBefore);
		assertFalse("Expected file not exists", newFile.exists());
		VFSEntry entry = newFile.createFile();
		assertTrue("Expected file exists", newFile.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write(fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue("Expected file exists", newFile.exists());

		entry.renameTo(fileNameAfter);

		assertFalse("Expected file not exists", newFile.exists());
		assertTrue("Expected file not exists", entry.getPath().exists());

		String readed = null;
		try (InputStream in = entry.getInputStream(); InputStreamReader reader = new InputStreamReader(in); BufferedReader br = new BufferedReader(reader)) {
			readed = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals("Expected readed content: " + fileContent, fileContent, readed);

	}

	@Test
	public void testCopyTo() throws VFSException {
		String copyFromFolder = "CopyFrom";
		String copyToFolder = "CopyTo";
		String copyFolder = "Copy";
		String copyFile = "copy.txt";
		String fileContent = "copy file";

		VFSEntry rootEntry = getVFSDiskManager().getRoot();
		VFSPath copyFromPath = rootEntry.getChildPath(copyFromFolder);
		assertFalse("Expected direcotry not exists", copyFromPath.exists());
		VFSEntry copyFromEntry = copyFromPath.createDirectory();
		assertTrue("Expected direcotry exists", copyFromPath.exists());

		VFSPath copyToPath = rootEntry.getChildPath(copyToFolder);
		assertFalse("Expected direcotry not exists", copyToPath.exists());

		VFSPath copyPath = copyFromEntry.getChildPath(copyFolder);
		assertFalse("Expected direcotry not exists", copyPath.exists());
		VFSEntry copyEntry = copyPath.createDirectory();
		assertTrue("Expected direcotry exists", copyPath.exists());

		VFSPath newFile = copyEntry.getChildPath(copyFile);
		VFSEntry entry = newFile.createFile();
		assertTrue("Expected file exists", newFile.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write(fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue("Expected file exists", newFile.exists());

		copyFromEntry.copyTo(copyToPath);
		assertTrue("Expected directory exists", copyToPath.exists());
		assertTrue("Expected directory exists", copyToPath.getVFSEntry().getChildPath(copyFolder).exists());
		assertTrue("Expected file exists", copyToPath.getVFSEntry().getChildPath(copyFolder).getVFSEntry().getChildPath(copyFile).exists());
		assertTrue("Expected directory exists", copyFromEntry.getChildPath(copyFolder).exists());
		assertTrue("Expected file exists", copyFromPath.getVFSEntry().getChildPath(copyFolder).getVFSEntry().getChildPath(copyFile).exists());
		entry = copyToPath.getVFSEntry().getChildPath(copyFolder).getVFSEntry().getChildPath(copyFile).getVFSEntry();
		String readed = null;
		try (InputStream in = entry.getInputStream(); InputStreamReader reader = new InputStreamReader(in); BufferedReader br = new BufferedReader(reader)) {
			readed = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals("Expected readed content: " + fileContent, fileContent, readed);
	}

	@Test
	public void testMoveTo() throws VFSException {
		String moveFromFolder = "MoveFrom";
		String moveToFolder = "MoveTo";
		String moveFolder = "Move";
		String moveFile = "move.txt";
		String fileContent = "move file";

		VFSEntry rootEntry = getVFSDiskManager().getRoot();
		VFSPath copyFromPath = rootEntry.getChildPath(moveFromFolder);
		assertFalse("Expected direcotry not exists", copyFromPath.exists());
		VFSEntry copyFromEntry = copyFromPath.createDirectory();
		assertTrue("Expected direcotry exists", copyFromPath.exists());

		VFSPath copyToPath = rootEntry.getChildPath(moveToFolder);
		assertFalse("Expected direcotry not exists", copyToPath.exists());

		VFSPath copyPath = copyFromEntry.getChildPath(moveFolder);
		assertFalse("Expected direcotry not exists", copyPath.exists());
		VFSEntry copyEntry = copyPath.createDirectory();
		assertTrue("Expected direcotry exists", copyPath.exists());

		VFSPath newFile = copyEntry.getChildPath(moveFile);
		VFSEntry entry = newFile.createFile();
		assertTrue("Expected file exists", newFile.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write(fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue("Expected file exists", newFile.exists());

		copyFromEntry.copyTo(copyToPath);
		assertTrue("Expected directory exists", copyToPath.exists());
		assertTrue("Expected directory exists", copyToPath.getVFSEntry().getChildPath(moveFolder).exists());
		assertTrue("Expected file exists", copyToPath.getVFSEntry().getChildPath(moveFolder).getVFSEntry().getChildPath(moveFile).exists());
		assertTrue("Expected directory exists", copyFromEntry.getChildPath(moveFolder).exists());
		assertTrue("Expected file exists", copyFromPath.getVFSEntry().getChildPath(moveFolder).getVFSEntry().getChildPath(moveFile).exists());
		entry = copyToPath.getVFSEntry().getChildPath(moveFolder).getVFSEntry().getChildPath(moveFile).getVFSEntry();
		String readed = null;
		try (InputStream in = entry.getInputStream(); InputStreamReader reader = new InputStreamReader(in); BufferedReader br = new BufferedReader(reader)) {
			readed = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals("Expected readed content: " + fileContent, fileContent, readed);
	}

	@Test
	public void testGetChildren() throws VFSException {
		getVFSDiskManager().dispose();
		Class<? extends VFSDiskManager> class1;
		try {
			class1 = (Class<? extends VFSDiskManager>) Class.forName(getVFSDiskManager().getClass().getName());
			Method method = class1.getMethod("create", DiskConfiguration.class);
			method.invoke(null, getVFSDiskManager().getDiskConfiguration());
			class1.getMethod("open", DiskConfiguration.class);
			setVFSDiskManager((VFSDiskManager) method.invoke(null, getVFSDiskManager().getDiskConfiguration()));
			assertTrue("Expected File to exist", new File(getVFSDiskManager().getDiskConfiguration().getHostFilePath()).exists());
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

		VFSEntry rootEntry = getVFSDiskManager().getRoot();
		VFSPath dir1Path = rootEntry.getChildPath(dir1);
		assertFalse("Expected directory not exists", dir1Path.exists());
		dir1Path.createDirectory();
		assertTrue("Expected directory exists", dir1Path.exists());

		VFSPath dir2Path = rootEntry.getChildPath(dir2);
		assertFalse("Expected directory not exists", dir2Path.exists());
		dir2Path.createDirectory();
		assertTrue("Expected directory exists", dir2Path.exists());

		VFSPath file1Path = rootEntry.getChildPath(file1);
		assertFalse("Expected file not exists", file1Path.exists());
		VFSEntry entry = file1Path.createFile();
		assertTrue("Expected file exists", file1Path.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write("Test\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<VFSEntry> childs = getVFSDiskManager().getRoot().getChildren();
		assertTrue(childs.size() == 3);
		for (VFSEntry child : childs) {
			System.out.println(child);
		}

	}

	@Test
	public void testDelete() throws VFSException {
		String delDir = "delDir";
		String delFile = "delFile.txt";

		VFSEntry rootEntry = getVFSDiskManager().getRoot();

		VFSPath delDirectoryPath = rootEntry.getChildPath(delDir);
		assertFalse("Expected directory not exists", delDirectoryPath.exists());
		VFSEntry delDirectoryEntry = delDirectoryPath.createDirectory();
		assertTrue("Expected directory exists", delDirectoryPath.exists());
		assertTrue("Expected isDirectory is true", delDirectoryEntry.isDirectory());

		VFSPath delFilePath = delDirectoryEntry.getChildPath(delFile);
		assertFalse("Expected file not exists", delFilePath.exists());
		VFSEntry delFileEntry = delFilePath.createFile();
		assertTrue("Expected file exists", delFilePath.exists());
		try {
			delDirectoryEntry.delete();
		} catch (VFSException e) {
			assertNotNull("Expected DirectoryNotEmptyException", e);
		}

		delFileEntry.delete();
		assertFalse("Expected file not exists", delFilePath.exists());
		delDirectoryEntry.delete();
		assertFalse("Expected directory not exists", delDirectoryPath.exists());
	}

	@Test
	public void testGetParent() throws VFSException {
		String testGetParent = "testGetParent";
		VFSEntry rootEntry = getVFSDiskManager().getRoot();

		VFSPath testGetParentPath = rootEntry.getChildPath(testGetParent);
		assertFalse("Expected directory not exists", testGetParentPath.exists());
		VFSEntry testGetParentEntry = testGetParentPath.createDirectory();
		assertTrue("Expected directory exists", testGetParentPath.exists());
		assertTrue("Expected isDirectory is true", testGetParentEntry.isDirectory());
		assertEquals(rootEntry.getPath().getAbsolutePath(), testGetParentEntry.getParent().getPath().getAbsolutePath());

	}

}
