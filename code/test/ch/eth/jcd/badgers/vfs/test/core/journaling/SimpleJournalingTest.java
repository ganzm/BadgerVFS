package ch.eth.jcd.badgers.vfs.test.core.journaling;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
		Journal journal = diskManager.closeAndGetCurrentJournal();

		journal.replay(secondDiskManager);

		// compare content of the file systems
		assertEntriesEqual(diskManager.getRoot(), secondDiskManager.getRoot());
	}

	private void assertEntriesEqual(VFSEntry expected, VFSEntry actual) throws VFSException {
		LOGGER.debug("comparing " + expected.getPath().getAbsolutePath());
		Assert.assertEquals(expected.isDirectory(), actual.isDirectory());
		Assert.assertEquals(expected.getPath().getName(), actual.getPath().getName());

		if (expected.isDirectory()) {
			List<VFSEntry> exptectedChildren = expected.getChildren();
			List<VFSEntry> actualChildren = actual.getChildren();
			Assert.assertEquals(exptectedChildren.size(), actualChildren.size());

			for (int i = 0; i < exptectedChildren.size(); i++) {
				VFSEntry exptedChild = exptectedChildren.get(i);
				VFSEntry actualChild = actualChildren.get(i);

				assertEntriesEqual(exptedChild, actualChild);
			}
		} else {
			// compare files
			assertFileContentEquals(expected, actual);
		}
	}

	/**
	 * Do not use this for big files
	 * 
	 * @param expected
	 * @param actual
	 */
	private void assertFileContentEquals(VFSEntry expected, VFSEntry actual) {
		try {
			byte[] exptecedContent = fileToBytes(expected);
			byte[] actualContent = fileToBytes(actual);

			Assert.assertArrayEquals("Files at " + expected.getPath().getAbsolutePath() + " differ", exptecedContent, actualContent);
		} catch (IOException | VFSException e) {
			Assert.fail(e.getMessage());
		}
	}

	private byte[] fileToBytes(VFSEntry entry) throws IOException, VFSException {
		byte[] buffer = new byte[512];
		int numBytes;

		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		try (InputStream in = entry.getInputStream()) {

			while ((numBytes = in.read(buffer, 0, buffer.length)) != -1) {
				bOut.write(buffer, 0, numBytes);
			}
		}

		return bOut.toByteArray();
	}
}
