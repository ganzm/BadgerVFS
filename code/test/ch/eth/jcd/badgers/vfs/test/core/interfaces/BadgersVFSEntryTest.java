package ch.eth.jcd.badgers.vfs.test.core.interfaces;

import static org.junit.Assert.assertFalse;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.test.testutil.UnitTestUtils;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;

public class BadgersVFSEntryTest extends IVFSEntryTest {

	private static VFSDiskManager manager = null;

	@BeforeClass
	public static void beforeClass() {
		UnittestLogger.init();
	}

	@Override
	public VFSDiskManager getVFSDiskManager() throws VFSException {
		if (manager == null) {
			manager = VFSDiskManagerImpl.create(UnitTestUtils.getMockedConfig("VFSPathTestMockedRoot"));
		}
		return manager;
	}

	@Override
	public void setVFSDiskManager(VFSDiskManager manager) throws VFSException {
		BadgersVFSEntryTest.manager = manager;
	}

	@AfterClass
	public static void afterClass() throws VFSException {
		manager.dispose();
		assertFalse("Expected File to be deleted", new File(manager.getDiskConfiguration().getHostFilePath()).exists());

	}

}