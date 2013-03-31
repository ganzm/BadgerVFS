package ch.eth.jcd.badgers.vfs.test.core;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.VFSDirectoryImpl;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.test.VFSDiskManagerTestBase;

public class DirectoryManipulationTest extends VFSDiskManagerTestBase {

	@Test
	public void testCreateSimpleDir() throws VFSException {

		NumberFormat decimalFormat = new DecimalFormat("###");

		VFSEntry rootEntry = diskManager.getRoot();

		for (int i = 0; i < 100; i++) {

			String folderName = "home" + decimalFormat.format(i);
			VFSPath path = rootEntry.getChildPath(folderName);
			Assert.assertFalse(path.exists());

			Assert.assertEquals("/" + folderName, path.getAbsolutePath());

			if (rootEntry instanceof VFSDirectoryImpl) {
				((VFSDirectoryImpl) rootEntry).debugPrint();
			}

			VFSEntry homeDir = path.createDirectory();

			if (rootEntry instanceof VFSDirectoryImpl) {
				((VFSDirectoryImpl) rootEntry).debugPrint();
			}

			Assert.assertTrue(path.exists());
			Assert.assertTrue(homeDir.isDirectory());
		}

		List<VFSEntry> children = rootEntry.getChildren();
		Assert.assertEquals(100, children.size());

		VFSEntry previous = null;
		for (VFSEntry entry : children) {

			if (previous != null) {

				String previousFileName = previous.getPath().getName();
				String fileName = entry.getPath().getName();

				Assert.assertTrue(previousFileName.compareTo(fileName) < 0);
			}

			previous = entry;
		}
	}
}
