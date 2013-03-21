package ch.eth.jcd.badgers.vfs.test.core;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;

public class SimpleVFSDiskManagerTest {

	private String fileName;

	@BeforeClass
	public static void beforeClass() {
		UnittestLogger.init();
	}

	@Before
	public void setup() {

		String tempDir = System.getProperty("java.io.tmpdir");

		// Delete if this file already exists
		fileName = tempDir + File.separatorChar + "test.tmp";
		if (new File(fileName).exists()) {
			new File(fileName).delete();
		}
	}

	@Test
	public void testCreateAndDispose() throws VFSException {
		DiskConfiguration config = new DiskConfiguration();
		config.setHostFilePath(fileName);

		VFSDiskManagerImpl instance = VFSDiskManagerImpl.create(config);

		Assert.assertTrue("Expected File to exist", new File(fileName).exists());

		instance.close();

		Assert.assertTrue("Expected File to exist", new File(fileName).exists());

		instance = VFSDiskManagerImpl.open(config);

		Assert.assertTrue("Expected File to exist", new File(fileName).exists());

		instance.dispose();

		Assert.assertFalse("Expected File to be deleted", new File(fileName).exists());
	}
}
