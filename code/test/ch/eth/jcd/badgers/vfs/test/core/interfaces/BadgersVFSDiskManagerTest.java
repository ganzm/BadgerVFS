package ch.eth.jcd.badgers.vfs.test.core.interfaces;

import static org.junit.Assert.assertFalse;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.test.testutil.UnitTestUtils;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;

public class BadgersVFSDiskManagerTest extends IVFSDiskManagerTest {

	private static VFSDiskManager manager = null;

	@BeforeClass
	public static void beforeClass() throws VFSException {
		UnittestLogger.init();
		initDiskManager();
	}

	@AfterClass
	public static void afterClass() throws VFSException {
		manager.dispose();
		assertFalse("Expected File to be deleted", new File(manager.getDiskConfiguration().getHostFilePath()).exists());

	}

	@Override
	public void setVFSDiskManager(VFSDiskManager manager) throws VFSException {
		if (BadgersVFSDiskManagerTest.manager != null) {
			BadgersVFSDiskManagerTest.manager.close();
			BadgersVFSDiskManagerTest.manager = null;
		}
		BadgersVFSDiskManagerTest.manager = manager;
	}

	private static void initDiskManager() throws VFSException {
		DiskConfiguration config = UnitTestUtils.getMockedConfig("BadgersVFSDiskManagerTest.bfs");

		UnitTestUtils.deleteFileIfExist(config.getHostFilePath());

		manager = VFSDiskManagerImpl.create(config);
	}

	@Override
	public VFSDiskManager getVFSDiskManager() throws VFSException {
		return manager;
	}
}
