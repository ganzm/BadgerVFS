package ch.eth.jcd.badgers.vfs.test.mock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

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

public class MockedVFSPathTest {

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
	public void testCreateDirectory() throws VFSException {
		VFSPath newDir = new MockedVFSPath("newDir", instance.getRoot());
		newDir.createDirectory();
		Assert.assertTrue(new File(getRootDir() + File.separatorChar + "newDir").exists());
	}

	@Test
	public void testCreateFile() throws VFSException {
		VFSPath newFile = new MockedVFSPath("newFile.txt", instance.getRoot());
		VFSEntry entry = newFile.createFile();
		Assert.assertTrue(!(new File(getRootDir() + File.separatorChar + "newFile.txt")).exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write("newFile 1");
		} catch (IOException e) {
			e.printStackTrace();
		}

		Assert.assertTrue(new File(getRootDir() + File.separatorChar + "newFile.txt").exists());

	}

	@Test
	public void testExists() throws VFSException {
		VFSPath existsDir = new MockedVFSPath("existsDir", instance.getRoot());
		Assert.assertFalse(existsDir.exists());
		existsDir.createDirectory();
		Assert.assertTrue(existsDir.exists());

	}

	@Test
	public void testGetPath() {
		MockedVFSPath newDir = new MockedVFSPath("newDir", instance.getRoot());
		String newDirPath = newDir.getPathString().substring(1);
		Assert.assertEquals("newDir", newDirPath);

		String newDirAbsolutePath = newDir.getAbsolutPath();
		Assert.assertEquals(((MockedVFSPath) instance.getRoot().getPath()).getAbsolutPath() + File.separatorChar + "newDir", newDirAbsolutePath);
	}

}
