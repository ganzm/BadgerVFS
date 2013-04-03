package ch.eth.jcd.badgers.vfs.test.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.test.VFSDiskManagerTestBase;
import ch.eth.jcd.badgers.vfs.test.testutil.CoreTestUtil;

public class DirectoryManipulationTest extends VFSDiskManagerTestBase {
	private static Logger LOGGER = Logger.getLogger(DirectoryManipulationTest.class);

	@Test
	public void testCreateSimpleDir() throws VFSException {

		NumberFormat decimalFormat = new DecimalFormat("###");
		int numEntries = 100;

		VFSEntry rootEntry = diskManager.getRoot();

		for (int i = 0; i < numEntries; i++) {

			String folderName = "home" + decimalFormat.format(i);
			VFSPath path = rootEntry.getChildPath(folderName);
			Assert.assertFalse(path.exists());

			Assert.assertEquals("/" + folderName, path.getAbsolutePath());

			CoreTestUtil.printDirBTree(rootEntry);
			VFSEntry homeDir = path.createDirectory();
			CoreTestUtil.printDirBTree(rootEntry);

			Assert.assertTrue(path.exists());
			Assert.assertTrue(homeDir.isDirectory());
		}

		List<VFSEntry> children = rootEntry.getChildren();
		Assert.assertEquals(numEntries, children.size());

		VFSEntry previous = null;
		for (VFSEntry entry : children) {

			if (previous != null) {

				String previousFileName = previous.getPath().getName();
				String fileName = entry.getPath().getName();

				assertTrue(previousFileName.compareTo(fileName) < 0);
			}

			previous = entry;
		}

		// ----------------------
		// delete the stuff we created

		for (int i = 0; i < numEntries; i++) {

			String folderName = "home" + decimalFormat.format(i);
			VFSPath path = rootEntry.getChildPath(folderName);

			assertTrue(path.exists());
			VFSEntry homeEntry = path.getVFSEntry();

			CoreTestUtil.printDirBTree(rootEntry);
			homeEntry.delete();
			CoreTestUtil.printDirBTree(rootEntry);

			assertFalse(path.exists());
		}

	}


	@Test
	public void testDelete() throws VFSException {
		// try to delete root directory
		VFSEntry rootEntry = diskManager.getRoot();

		VFSPath subFolderPath = rootEntry.getChildPath("subfolder1");

		Assert.assertFalse(subFolderPath.exists());
		VFSEntry subfolder = subFolderPath.createDirectory();
		Assert.assertTrue(subFolderPath.exists());

		subfolder.delete();

		subFolderPath = rootEntry.getChildPath("subfolder1");
		Assert.assertFalse(subFolderPath.exists());

		try {
			rootEntry.delete();
			Assert.fail("expected an exception");
		} catch (VFSException e) {
			LOGGER.info("Expected this: ", e);
		}
	}
}
