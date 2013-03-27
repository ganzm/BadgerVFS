package ch.eth.jcd.badgers.vfs.test.core.interfaces;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Method;

import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public abstract class IVFSDiskManagerTest {

	public abstract VFSDiskManager getVFSDiskManager() throws VFSException;

	@Test
	public void testCreateAndDispose() throws VFSException {
		assertTrue("Expected File to exist", new File(getVFSDiskManager().getDiskConfiguration().getHostFilePath()).exists());

		getVFSDiskManager().close();

		assertTrue("Expected File to exist", new File(getVFSDiskManager().getDiskConfiguration().getHostFilePath()).exists());

		Class<? extends VFSDiskManager> class1;
		try {
			class1 = (Class<? extends VFSDiskManager>) Class.forName(getVFSDiskManager().getClass().getName());
			Method method = class1.getMethod("open", DiskConfiguration.class);
			Object o = method.invoke(null, getVFSDiskManager().getDiskConfiguration());
			assertTrue("Expected File to exist", new File(getVFSDiskManager().getDiskConfiguration().getHostFilePath()).exists());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testgetRoot() throws VFSException {
		assertTrue("Expected File to exist", new File(getVFSDiskManager().getDiskConfiguration().getHostFilePath()).exists());
		VFSEntry entry = getVFSDiskManager().getRoot();
		assertTrue("Expected Root Entry to exist", entry.getPath().exists());
	}
}
