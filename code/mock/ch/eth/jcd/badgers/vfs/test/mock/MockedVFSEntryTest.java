package ch.eth.jcd.badgers.vfs.test.mock;

import static org.junit.Assert.assertFalse;

import java.io.File;

import org.junit.AfterClass;

import ch.eth.jcd.badgers.vfs.core.interfaces.FindInFolderObserver;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.mock.MockedFindInFolderObserver;
import ch.eth.jcd.badgers.vfs.mock.MockedVFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.test.core.interfaces.IVFSEntryTest;
import ch.eth.jcd.badgers.vfs.test.testutil.UnitTestUtils;

public class MockedVFSEntryTest extends IVFSEntryTest {

	private static VFSDiskManager manager = null;

	@Override
	public VFSDiskManager getVFSDiskManager() throws VFSException {
		if (manager == null) {
			manager = MockedVFSDiskManagerImpl.create(UnitTestUtils.getMockedConfig("VFSPathTestMockedRoot"));
		}
		return manager;
	}

	@Override
	public void setVFSDiskManager(VFSDiskManager manager) throws VFSException {
		MockedVFSEntryTest.manager = manager;
	}

	@AfterClass
	public static void afterClass() throws VFSException {
		manager.dispose();
		assertFalse("Expected File to be deleted", new File(manager.getDiskConfiguration().getHostFilePath()).exists());

	}

	@Override
	public FindInFolderObserver getFindInFolderObserver() throws VFSException {
		return new MockedFindInFolderObserver();
	}

}