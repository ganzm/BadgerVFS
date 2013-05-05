package ch.eth.jcd.badgers.vfs.test.testutil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;

import ch.eth.jcd.badgers.vfs.core.VFSDirectoryImpl;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class CoreTestUtil {
	private static final Logger LOGGER = Logger.getLogger(CoreTestUtil.class);

	public static void printDirBTree(VFSEntry directory) {
		try {

			if (directory instanceof VFSDirectoryImpl) {
				((VFSDirectoryImpl) directory).debugPrint();

				StringBuffer buf = new StringBuffer();
				boolean result = ((VFSDirectoryImpl) directory).performTreeSanityCheck(buf);
				Assert.assertTrue(buf.toString(), result);
			}
		} catch (VFSException e) {
			LOGGER.error("", e);
			Assert.fail(e.getMessage());
		}
	}

	public static void assertEntriesEqual(VFSEntry expected, VFSEntry actual) throws VFSException {
		LOGGER.debug("comparing " + expected.getPath().getAbsolutePath());
		Assert.assertEquals(expected.isDirectory(), actual.isDirectory());
		Assert.assertEquals(expected.getPath().getName(), actual.getPath().getName());

		if (expected.isDirectory()) {
			List<VFSEntry> exptectedChildren = expected.getChildren();
			List<VFSEntry> actualChildren = actual.getChildren();
			Assert.assertEquals("" + expected.getPath().getAbsolutePath() + " expected children " + exptectedChildren, exptectedChildren.size(),
					actualChildren.size());

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
	public static void assertFileContentEquals(VFSEntry expected, VFSEntry actual) {
		try {
			byte[] exptecedContent = fileToBytes(expected);
			byte[] actualContent = fileToBytes(actual);

			Assert.assertArrayEquals("Files at " + expected.getPath().getAbsolutePath() + " differ", exptecedContent, actualContent);
		} catch (IOException | VFSException e) {
			Assert.fail(e.getMessage());
		}
	}

	public static byte[] fileToBytes(VFSEntry entry) throws IOException, VFSException {
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
