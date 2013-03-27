package ch.eth.jcd.badgers.vfs.test.core;

import junit.framework.Assert;

import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.test.VFSDiskManagerTestBase;

public class DirectoryManipulationTest extends VFSDiskManagerTestBase {

	@Test
	public void testCreateSimpleDir() throws VFSException {

		VFSPath path = diskManager.CreatePath("/home");
		Assert.assertFalse(path.exists());

		VFSEntry homeDir = path.createDirectory();
		Assert.assertTrue(path.exists());
	}
}
