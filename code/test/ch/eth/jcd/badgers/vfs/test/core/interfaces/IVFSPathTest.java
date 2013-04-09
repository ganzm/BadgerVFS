package ch.eth.jcd.badgers.vfs.test.core.interfaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public abstract class IVFSPathTest {

	public abstract VFSDiskManagerFactory getVFSDiskManagerFactory() throws VFSException;

	public abstract DiskConfiguration getConfiguration() throws VFSException;

	public abstract void setVFSDiskManager(VFSDiskManager manager) throws VFSException;

	protected VFSDiskManager diskManager;

	@Before
	public void beforeTest() throws VFSException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		DiskConfiguration config = getConfiguration();
		String filePath = config.getHostFilePath();
		VFSDiskManagerFactory factory = getVFSDiskManagerFactory();
		diskManager = factory.openDiskManager(config);
		setVFSDiskManager(diskManager);
		assertTrue("Expected File to exist", new File(filePath).exists());
	}

	@After
	public void afterTest() throws VFSException {
		diskManager.close();
	}

	@Test
	public void testCreateDirectory() throws VFSException {
		VFSEntry rootEntry = diskManager.getRoot();
		VFSPath testDir = rootEntry.getChildPath("dirTest");
		assertFalse("Expected directory not exists", testDir.exists());
		testDir.createDirectory();
		assertTrue("Expected directory exists", testDir.exists());
	}

	@Test
	public void testCreateFile() throws VFSException {
		VFSEntry rootEntry = diskManager.getRoot();

		VFSPath newFile = rootEntry.getChildPath("newFile.txt");
		assertFalse("Expected file not exists", newFile.exists());
		VFSEntry entry = newFile.createFile();
		assertTrue("Expected file exists", entry.getPath().exists());
	}

	@Test
	public void testExists() throws VFSException {
		VFSEntry rootEntry = diskManager.getRoot();
		VFSPath newFile = rootEntry.getChildPath("newExists.txt");
		assertFalse("Expected file not exists", newFile.exists());
		VFSEntry entry = newFile.createFile();
		assertTrue("Expected file exists", entry.getPath().exists());

	}

	@Test
	public void testGetPathString() throws VFSException {
		VFSEntry rootEntry = diskManager.getRoot();
		VFSPath newDir = rootEntry.getChildPath("newDir");
		String newDirPath = newDir.getAbsolutePath();
		assertEquals("Expected path = /newDir", "/newDir", newDirPath);
	}

	@Test
	public void testGetName() throws VFSException {
		String newName = "newName";
		VFSEntry rootEntry = diskManager.getRoot();
		assertEquals("Expected name = \"\"", "", rootEntry.getPath().getName());
		VFSPath newDirPath = rootEntry.getChildPath(newName);
		assertEquals("Expected name = \"newName\"", newName, newDirPath.getName());
		VFSEntry newDirEntry = newDirPath.createDirectory();
		assertEquals("Expected name = \"newName\"", newName, newDirEntry.getPath().getName());
	}

	@Test
	public void testGetParentPath() throws VFSException {
		String childFromParent = "childFromParent";
		String fileChildFromParent = "childFromParent.txt";
		VFSEntry rootEntry = diskManager.getRoot();
		assertEquals("Expected parentPath = \"/\"", "/", rootEntry.getPath().getParentPath());
		VFSPath newDirPath = rootEntry.getChildPath(childFromParent);
		assertEquals("Expected name = \"/\"", "/", newDirPath.getParentPath());
		VFSEntry newDirEntry = newDirPath.createDirectory();
		assertEquals("Expected still name = \"/\"", "/", newDirEntry.getPath().getParentPath());
		VFSPath childFromParentFilePath = newDirEntry.getChildPath(fileChildFromParent);
		assertEquals("Expected name = \"childFromParent\"", "/childFromParent", childFromParentFilePath.getParentPath());

	}

}
