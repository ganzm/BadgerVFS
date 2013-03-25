package ch.eth.jcd.badgers.vfs.test.mock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.mock.MockedVFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.mock.MockedVFSPath;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;

public class MockedVFSEntryTest {

	@AfterClass
	public static void afterClass() throws VFSException {
		MockedVFSDiskManagerImpl instance = MockedVFSDiskManagerImpl.open(getMockedConfig());
		instance.dispose();

	}

	@BeforeClass
	public static void beforeClass() throws VFSException {
		UnittestLogger.init();
		MockedVFSDiskManagerImpl.create(getMockedConfig());

	}

	private static DiskConfiguration getMockedConfig() {
		DiskConfiguration config = new DiskConfiguration();
		config.setHostFilePath(getRootDir());
		return config;

	}

	private static String getRootDir() {
		String tempDir = System.getProperty("java.io.tmpdir");
		String fileName;
		if (tempDir.endsWith("/") || tempDir.endsWith("\\")) {
			fileName = tempDir + "mocked";
		} else {
			fileName = tempDir + File.separatorChar + "mocked";
		}
		return fileName;

	}

	private MockedVFSDiskManagerImpl instance;

	@Before
	public void setup() throws VFSException {

		instance = MockedVFSDiskManagerImpl.open(getMockedConfig());

	}

	@Test
	public void testIsDirectory() throws VFSException {
		String isDirectory = "IsDirectory";
		String IsNotADirectory = "IsNotADirectory.txt";

		VFSPath isDirectoryPath = new MockedVFSPath(isDirectory, instance.getRoot());
		Assert.assertFalse(isDirectoryPath.exists());
		isDirectoryPath.createDirectory();
		Assert.assertTrue(isDirectoryPath.exists());
		Assert.assertTrue(isDirectoryPath.getVFSEntry().isDirectory());

		VFSPath aFileNotDirectory = new MockedVFSPath(isDirectory + File.separatorChar + IsNotADirectory, instance.getRoot());
		VFSEntry entry = aFileNotDirectory.createFile();
		Assert.assertTrue(!(new File(getRootDir() + File.separatorChar + isDirectory + File.separatorChar + IsNotADirectory)).exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write("Copy Test");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertTrue(new File(getRootDir() + File.separatorChar + isDirectory + File.separatorChar + IsNotADirectory).exists());
		Assert.assertFalse(entry.isDirectory());

	}

	@Test
	public void testGetOutputStreamTest() throws VFSException {
		VFSPath newFile = new MockedVFSPath("newOutputStreamTest.txt", instance.getRoot());
		VFSEntry entry = newFile.createFile();
		Assert.assertTrue(!(new File(getRootDir() + File.separatorChar + "newOutputStreamTest.txt")).exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write("newOutputStreamTest String");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertTrue(new File(getRootDir() + File.separatorChar + "newOutputStreamTest.txt").exists());
	}

	@Test
	public void testGetInputStreamTest() throws VFSException {
		String fileName = "newInputStreamTest.txt";
		String fileContent = "newInputStreamTest String";
		VFSPath newFile = new MockedVFSPath(fileName, instance.getRoot());
		VFSEntry entry = newFile.createFile();
		Assert.assertTrue(!(new File(getRootDir() + File.separatorChar + fileName)).exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write(fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertTrue(new File(getRootDir() + File.separatorChar + fileName).exists());

		String readed = null;
		try (InputStream in = entry.getInputStream(); InputStreamReader reader = new InputStreamReader(in); BufferedReader br = new BufferedReader(reader)) {
			readed = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(fileContent, readed);
	}

	@Test
	public void testRenameTo() throws VFSException {
		String fileNameBefore = "newbeforeRename.txt";
		String fileNameAfter = "newafterRename.txt";
		String fileContent = "Test String\n";
		VFSPath newFile = new MockedVFSPath(fileNameBefore, instance.getRoot());
		VFSEntry entry = newFile.createFile();
		Assert.assertTrue(!(new File(getRootDir() + File.separatorChar + fileNameBefore)).exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write(fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertTrue(new File(getRootDir() + File.separatorChar + fileNameBefore).exists());

		entry.renameTo(fileNameAfter);

		Assert.assertFalse(new File(getRootDir() + File.separatorChar + fileNameBefore).exists());
		Assert.assertTrue(new File(getRootDir() + File.separatorChar + fileNameAfter).exists());
	}

	@Test
	public void testCopyTo() throws VFSException {
		String copyFromFolder = "CopyFrom";
		String copyToFolder = "CopyTo";
		String testFolder = "Copy";
		String testFile = "copy.txt";

		VFSPath copyFromPath = new MockedVFSPath(copyFromFolder, instance.getRoot());
		Assert.assertFalse(copyFromPath.exists());
		copyFromPath.createDirectory();
		Assert.assertTrue(copyFromPath.exists());

		VFSPath copyToPath = new MockedVFSPath(copyToFolder, instance.getRoot());
		Assert.assertFalse(copyToPath.exists());

		VFSPath copyPath = new MockedVFSPath(copyFromFolder + File.separatorChar + testFolder, instance.getRoot());
		Assert.assertFalse(copyPath.exists());
		copyPath.createDirectory();
		Assert.assertTrue(copyPath.exists());

		VFSPath newFile = new MockedVFSPath(copyFromFolder + File.separatorChar + testFolder + File.separatorChar + testFile, instance.getRoot());
		VFSEntry entry = newFile.createFile();
		Assert.assertTrue(!(new File(getRootDir() + File.separatorChar + copyFromFolder + File.separatorChar + testFolder + File.separatorChar + testFile))
				.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write("Copy Test");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertTrue(new File(getRootDir() + File.separatorChar + copyFromFolder + File.separatorChar + testFolder + File.separatorChar + testFile)
				.exists());

		copyFromPath.getVFSEntry().copyTo(copyToPath);
		Assert.assertTrue(copyPath.exists());
		Assert.assertTrue(copyToPath.exists());
		Assert.assertTrue(new MockedVFSPath(copyToFolder + File.separatorChar + testFolder, instance.getRoot()).exists());
		Assert.assertTrue(new File(getRootDir() + File.separatorChar + copyToFolder + File.separatorChar + testFolder + File.separatorChar + testFile).exists());
	}

	@Test
	public void testMoveTo() throws VFSException {
		String copyFromFolder = "MoveFrom";
		String copyToFolder = "MoveTo";
		String testFolder = "Move";
		String testFile = "move.txt";

		VFSPath copyFromPath = new MockedVFSPath(copyFromFolder, instance.getRoot());
		Assert.assertFalse(copyFromPath.exists());
		copyFromPath.createDirectory();
		Assert.assertTrue(copyFromPath.exists());

		VFSPath copyToPath = new MockedVFSPath(copyToFolder, instance.getRoot());
		Assert.assertFalse(copyToPath.exists());

		VFSPath copyPath = new MockedVFSPath(copyFromFolder + File.separatorChar + testFolder, instance.getRoot());
		Assert.assertFalse(copyPath.exists());
		copyPath.createDirectory();
		Assert.assertTrue(copyPath.exists());

		VFSPath newFile = new MockedVFSPath(copyFromFolder + File.separatorChar + testFolder + File.separatorChar + testFile, instance.getRoot());
		VFSEntry entry = newFile.createFile();
		Assert.assertTrue(!(new File(getRootDir() + File.separatorChar + copyFromFolder + File.separatorChar + testFolder + File.separatorChar + testFile))
				.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write("Move Test");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertTrue(new File(getRootDir() + File.separatorChar + copyFromFolder + File.separatorChar + testFolder + File.separatorChar + testFile)
				.exists());

		copyFromPath.getVFSEntry().moveTo(copyToPath);
		Assert.assertFalse(copyPath.exists());
		Assert.assertTrue(copyToPath.exists());
		Assert.assertTrue(new MockedVFSPath(copyToFolder + File.separatorChar + testFolder, instance.getRoot()).exists());
		Assert.assertTrue(new File(getRootDir() + File.separatorChar + copyToFolder + File.separatorChar + testFolder + File.separatorChar + testFile).exists());
	}

	@Test
	public void testGetChildren() throws VFSException {
		instance.dispose();
		try {
			MockedVFSDiskManagerImpl.create(getMockedConfig());
			instance = MockedVFSDiskManagerImpl.open(getMockedConfig());
		} catch (VFSException e1) {
			e1.printStackTrace();
		}
		String dir1 = "Dir1";
		String dir2 = "Dir2";
		String file1 = "File1.txt";

		VFSPath dir1Path = new MockedVFSPath(dir1, instance.getRoot());
		Assert.assertFalse(dir1Path.exists());
		dir1Path.createDirectory();
		Assert.assertTrue(dir1Path.exists());

		VFSPath dir2Path = new MockedVFSPath(dir2, instance.getRoot());
		Assert.assertFalse(dir2Path.exists());
		dir2Path.createDirectory();
		Assert.assertTrue(dir2Path.exists());

		VFSPath file1Path = new MockedVFSPath(file1, instance.getRoot());
		VFSEntry entry = file1Path.createFile();
		Assert.assertTrue(!(new File(getRootDir() + File.separatorChar + file1)).exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write("Test\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertTrue(new File(getRootDir() + File.separatorChar + file1).exists());

		List<VFSEntry> childs = instance.getRoot().getChildren();
		Assert.assertTrue(childs.size() == 3);
		for (VFSEntry child : childs) {
			System.out.println(child);
		}

	}

}
