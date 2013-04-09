package ch.eth.jcd.badgers.vfs.test.mock;

import static org.junit.Assert.assertFalse;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.mock.MockedVFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.mock.VFSMockDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.test.core.interfaces.IVFSDiskManagerTest;
import ch.eth.jcd.badgers.vfs.test.testutil.UnitTestUtils;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;

public class MockedVFSDiskManagerImplTest extends IVFSDiskManagerTest {

	private static VFSDiskManager manager = null;

	@AfterClass
	public static void afterClass() throws VFSException {
		manager.dispose();
		assertFalse("Expected File to be deleted", new File(manager.getDiskConfiguration().getHostFilePath()).exists());
	}

	@BeforeClass
	public static void beforeClass() throws VFSException {
		UnittestLogger.init();
		initDiskManager();
	}

	private static void initDiskManager() throws VFSException {
		DiskConfiguration config = UnitTestUtils.getMockedConfig("VFSDiskManagerTestMocked/");
		UnitTestUtils.deleteFileIfExist(config.getHostFilePath());
		manager = MockedVFSDiskManagerImpl.create(config);
	}

	@Override
	public void setVFSDiskManager(VFSDiskManager manager) throws VFSException {
		MockedVFSDiskManagerImplTest.manager = manager;
	}

	@Override
	public VFSDiskManagerFactory getVFSDiskManagerFactory() throws VFSException {
		return new VFSMockDiskManagerFactory();
	}

	@Override
	public DiskConfiguration getConfiguration() throws VFSException {
		return UnitTestUtils.getMockedConfig("VFSDiskManagerTestMocked");
	}
}
