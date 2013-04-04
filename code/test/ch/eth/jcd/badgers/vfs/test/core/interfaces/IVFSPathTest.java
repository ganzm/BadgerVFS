package ch.eth.jcd.badgers.vfs.test.core.interfaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public abstract class IVFSPathTest {

	public abstract VFSDiskManager getVFSDiskManager() throws VFSException;

	public abstract void setVFSDiskManager(VFSDiskManager manager) throws VFSException;

	@Before
	public void beforeTest() throws VFSException {
		Class<? extends VFSDiskManager> class1;
		try {
			class1 = Class.forName(getVFSDiskManager().getClass().getName()).asSubclass(VFSDiskManager.class);
			Method methodOpen = class1.getMethod("open", DiskConfiguration.class);
			setVFSDiskManager((VFSDiskManager) methodOpen.invoke(null, getVFSDiskManager().getDiskConfiguration()));
			assertTrue("Expected File to exist", new File(getVFSDiskManager().getDiskConfiguration().getHostFilePath()).exists());
		} catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new VFSException(e);
		}

	}

	@After
	public void afterTest() throws VFSException {
		getVFSDiskManager().close();
	}

	@Test
	public void testCreateDirectory() throws VFSException {
		VFSEntry rootEntry = getVFSDiskManager().getRoot();
		VFSPath testDir = rootEntry.getChildPath("dirTest");
		assertFalse("Expected directory not exists", testDir.exists());
		testDir.createDirectory();
		assertTrue("Expected directory exists", testDir.exists());
	}

	@Test
	public void testCreateFile() throws VFSException {
		VFSEntry rootEntry = getVFSDiskManager().getRoot();

		VFSPath newFile = rootEntry.getChildPath("newFile.txt");
		assertFalse("Expected file not exists", newFile.exists());
		VFSEntry entry = newFile.createFile();
		assertTrue("Expected file exists", entry.getPath().exists());
	}

	@Test
	public void testExists() throws VFSException {
		VFSEntry rootEntry = getVFSDiskManager().getRoot();
		VFSPath newFile = rootEntry.getChildPath("newExists.txt");
		assertFalse("Expected file not exists", newFile.exists());
		VFSEntry entry = newFile.createFile();
		assertTrue("Expected file exists", entry.getPath().exists());

	}

	@Test
	public void testGetPathString() throws VFSException {
		VFSEntry rootEntry = getVFSDiskManager().getRoot();
		VFSPath newDir = rootEntry.getChildPath("newDir");
		String newDirPath = newDir.getAbsolutePath();
		assertEquals("Expected path = /newDir", "/newDir", newDirPath);
	}

	@Test
	public void testGetName() throws VFSException {
		String newName = "newName";
		VFSEntry rootEntry = getVFSDiskManager().getRoot();
		assertEquals("Expected name = \"\"", "", rootEntry.getPath().getName());
		VFSPath newDirPath = rootEntry.getChildPath(newName);
		assertEquals("Expected name = \"newName\"", newName, newDirPath.getName());
		VFSEntry newDirEntry = newDirPath.createDirectory();
		assertEquals("Expected name = \"newName\"", newName, newDirEntry.getPath().getName());
	}

}
