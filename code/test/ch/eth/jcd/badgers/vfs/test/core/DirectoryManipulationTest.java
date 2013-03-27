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

		VFSEntry rootPath = diskManager.getRoot();
		VFSPath path = rootPath.getChildPath("home");
		Assert.assertFalse(path.exists());
		Assert.assertEquals("/home", path.getAbsolutePath());

		VFSEntry homeDir = path.createDirectory();
		Assert.assertTrue(path.exists());
		Assert.assertTrue(homeDir.isDirectory());
	}
}
