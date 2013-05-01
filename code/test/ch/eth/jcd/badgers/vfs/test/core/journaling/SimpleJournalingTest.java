package ch.eth.jcd.badgers.vfs.test.core.journaling;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.test.VFSDiskManagerTestBase;
import ch.eth.jcd.badgers.vfs.test.testutil.CoreTestUtil;

public class SimpleJournalingTest extends VFSDiskManagerTestBase {

	private static final Logger LOGGER = Logger.getLogger(SimpleJournalingTest.class);

	private VFSDiskManager secondDiskManager;

	@Before
	public void before() throws VFSException {
		diskManager.dispose();
		diskManager = setupDefault("test.tmp", "localhost");

		secondDiskManager = setupDefault("test2.tmp", "localhost");
	}

	@After
	public void after() throws VFSException {
		secondDiskManager.dispose();
	}

	@Test
	public void testReplayJournal() throws VFSException, IOException {
		LOGGER.info("Test Replay");

		VFSEntry root = diskManager.getRoot();

		LOGGER.info("Create Dir");
		VFSEntry subDir = root.getChildPath("SubDirectory").createDirectory();
		VFSEntry subDir2 = root.getChildPath("SubDirectory2").createDirectory();

		LOGGER.info("Create Temp file");
		VFSEntry toDeleteFile = subDir.getChildPath("tmp.txt").createFile();
		try (OutputStream out = toDeleteFile.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
			out.write("file to delete content".getBytes());
		}

		LOGGER.info("Create other file");
		VFSEntry otherFile = subDir.getChildPath("otherfile.txt").createFile();
		try (OutputStream out = otherFile.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
			out.write("otherfile content".getBytes());
		}

		LOGGER.info("Delete file");
		toDeleteFile.delete();

		LOGGER.info("Rename other file");
		otherFile.renameTo("test.txt");
		otherFile.copyTo(subDir2.getChildPath("test.txt"));
		otherFile.moveTo(root.getChildPath("renamed.txt"));

		LOGGER.info("Try to replay Journal on the second Disk");
		diskManager.closeCurrentJournal();
		List<Journal> journals = diskManager.getPendingJournals();
		Assert.assertEquals(1, journals.size());

		Journal journal = journals.get(0);

		journal.beforeLocalTransport(diskManager);

		journal.replay(secondDiskManager);

		// compare content of the file systems
		CoreTestUtil.assertEntriesEqual(diskManager.getRoot(), secondDiskManager.getRoot());
	}

}
