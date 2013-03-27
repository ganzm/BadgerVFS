package ch.eth.jcd.badgers.vfs.test.mock;

import static org.junit.Assert.assertFalse;

import java.io.File;

import org.junit.AfterClass;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.mock.MockedVFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.test.core.interfaces.IVFSDiskManagerTest;
import ch.eth.jcd.badgers.vfs.test.testutil.UnitTestUtils;

public class MockedVFSDiskManagerImplTest extends IVFSDiskManagerTest {

	@AfterClass
	public static void afterClass() throws VFSException {

		manager.dispose();
		assertFalse("Expected File to be deleted", new File(manager.getDiskConfiguration().getHostFilePath()).exists());

	}

	private static MockedVFSDiskManagerImpl manager = null;

	@Override
	public VFSDiskManager getVFSDiskManager() throws VFSException {
		if (manager == null) {
			manager = MockedVFSDiskManagerImpl.create(UnitTestUtils.getMockedConfig("VFSDiskManagerTestMocked"));
		}
		return manager;
	}
}
