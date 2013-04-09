package ch.eth.jcd.badgers.vfs.test.core.interfaces;

import static org.junit.Assert.assertFalse;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImplFactory;
import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.test.testutil.UnitTestUtils;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;

public class BadgersVFSPathTest extends IVFSPathTest {

	private static VFSDiskManager manager = null;

	@BeforeClass
	public static void beforeClass() throws VFSException {
		UnittestLogger.init();
		initDiskManager();
	}

	@Override
	public VFSDiskManagerFactory getVFSDiskManagerFactory() throws VFSException {
		return new VFSDiskManagerImplFactory();
	}

	@Override
	public DiskConfiguration getConfiguration() throws VFSException {
		return UnitTestUtils.getMockedConfig("BadgersVFSPathTest.bfs");
	}

	@Override
	public void setVFSDiskManager(VFSDiskManager manager) throws VFSException {
		if (BadgersVFSPathTest.manager != null) {
			BadgersVFSPathTest.manager.close();
			BadgersVFSPathTest.manager = null;
		}

		BadgersVFSPathTest.manager = manager;
	}

	private static void initDiskManager() throws VFSException {
		DiskConfiguration config = UnitTestUtils.getMockedConfig("BadgersVFSPathTest.bfs");
		UnitTestUtils.deleteFileIfExist(config.getHostFilePath());
		manager = VFSDiskManagerImpl.create(config);
	}

	@AfterClass
	public static void afterClass() throws VFSException {
		manager.dispose();
		assertFalse("Expected File to be deleted", new File(manager.getDiskConfiguration().getHostFilePath()).exists());

	}

}