package ch.eth.jcd.badgers.vfs.test.core.interfaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

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
public class VFSPathTest {

	private static VFSDiskManager diskManager;

	public VFSPathTest(VFSDiskManager manager) {
		VFSPathTest.diskManager = manager;
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
			assertTrue("Expected File to exist", new File(diskManager.getDiskConfiguration().getHostFilePath()).exists());
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

	@Parameters(name = "Run {0}")
	public static Collection<Object[]> getParameters() throws VFSException {
		return Arrays.asList(new Object[][] { { MockedVFSDiskManagerImpl.create(getMockedConfig("VFSPathTestMockedRoot")) },
				{ VFSDiskManagerImpl.create(getMockedConfig("VFSPathTestBadgersDisk.bfs")) } });
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

}
