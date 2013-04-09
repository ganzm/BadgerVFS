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
import ch.eth.jcd.badgers.vfs.test.core.interfaces.IVFSEntryTest;
import ch.eth.jcd.badgers.vfs.test.testutil.UnitTestUtils;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;

public class MockedVFSEntryTest extends IVFSEntryTest {

	private static VFSDiskManager manager = null;

	@BeforeClass
	public static void beforeClass() throws VFSException {
		UnittestLogger.init();
		initDiskManager();
	}

	@Override
	public VFSDiskManagerFactory getVFSDiskManagerFactory() throws VFSException {
		return new VFSMockDiskManagerFactory();
	}

	@Override
	public DiskConfiguration getConfiguration() throws VFSException {
		return UnitTestUtils.getMockedConfig("VFSEntryTestMockedRoot");
	}

	@Override
	public void setVFSDiskManager(VFSDiskManager manager) throws VFSException {
		MockedVFSEntryTest.manager = manager;
	}

	private static void initDiskManager() throws VFSException {
		DiskConfiguration config = UnitTestUtils.getMockedConfig("VFSEntryTestMockedRoot");
		UnitTestUtils.deleteFileIfExist(config.getHostFilePath());
		manager = MockedVFSDiskManagerImpl.create(config);
	}

	@AfterClass
	public static void afterClass() throws VFSException {
		manager.dispose();
		assertFalse("Expected File to be deleted", new File(manager.getDiskConfiguration().getHostFilePath()).exists());

	}

}