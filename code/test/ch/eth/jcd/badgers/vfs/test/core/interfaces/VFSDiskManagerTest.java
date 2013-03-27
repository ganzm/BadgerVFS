package ch.eth.jcd.badgers.vfs.test.core.interfaces;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.mock.MockedVFSDiskManagerImpl;

@RunWith(Parameterized.class)
public class VFSDiskManagerTest {

	private static VFSDiskManager diskManager;

	public VFSDiskManagerTest(VFSDiskManager manager) {
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
		assertFalse("Expected File to be deleted", new File(diskManager.getDiskConfiguration().getHostFilePath()).exists());

	}

	@Test
	public void testCreateAndDispose() throws VFSException {
		assertTrue("Expected File to exist", new File(diskManager.getDiskConfiguration().getHostFilePath()).exists());

		diskManager.close();

		assertTrue("Expected File to exist", new File(diskManager.getDiskConfiguration().getHostFilePath()).exists());

		Class<? extends VFSDiskManager> class1;
		try {
			class1 = (Class<? extends VFSDiskManager>) Class.forName(diskManager.getClass().getName());
			Method method = class1.getMethod("create", DiskConfiguration.class);
			Object o = method.invoke(null, diskManager.getDiskConfiguration());
			System.out.println(o);
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

	@Test
	public void testgetRoot() throws VFSException {
		assertTrue("Expected File to exist", new File(diskManager.getDiskConfiguration().getHostFilePath()).exists());
		VFSEntry entry = diskManager.getRoot();
		assertTrue(entry.getPath().exists());
	}

	@Parameters(name = "Run {0}")
	public static Collection<Object[]> getParameters() throws VFSException {
		return Arrays.asList(new Object[][] { { MockedVFSDiskManagerImpl.create(getMockedConfig("VFSDiskManagerTestMocked")) },
				{ VFSDiskManagerImpl.create(getMockedConfig("VFSDiskManagerTestBadgersDisk.bfs")) } });
	}

}
