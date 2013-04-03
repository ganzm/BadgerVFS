package ch.eth.jcd.badgers.vfs.test.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.test.VFSDiskManagerTestBase;
import ch.eth.jcd.badgers.vfs.test.testutil.CoreTestUtil;

public class FileManipulationTest extends VFSDiskManagerTestBase {
	private static Logger LOGGER = Logger.getLogger(FileManipulationTest.class);

	private final Random rnd = new Random(0);

	@Test
	public void testCreateSimpleFiles() throws VFSException, IOException {

		NumberFormat decimalFormat = new DecimalFormat("000");
		int numEntries = 100;

		VFSEntry rootEntry = diskManager.getRoot();

		// create home folder
		String folderName = "home";
		VFSPath path = rootEntry.getChildPath(folderName);
		VFSEntry homeDir = path.createDirectory();

		// ---------------------------------
		// creation
		// ---------------------------------

		for (int i = 0; i < numEntries; i++) {

			String fileName = "file" + decimalFormat.format(i);
			VFSPath filePath = homeDir.getChildPath(fileName);

			LOGGER.debug("Create " + fileName);
			Assert.assertFalse(filePath.exists());
			VFSEntry fileEntry = filePath.createFile();
			CoreTestUtil.printDirBTree(homeDir);
			Assert.assertTrue(filePath.exists());

			InputStream in = fileEntry.getInputStream();
			Assert.assertEquals("Expect empty stream", -1, in.read());
			in.close();

			OutputStream out = fileEntry.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE);

			byte[] rawData = generateRandomData();
			out.write(rawData);
			out.close();

			in = fileEntry.getInputStream();

			ByteArrayOutputStream byteOut = new ByteArrayOutputStream(rawData.length);
			int numData;
			byte[] buffer = new byte[512];
			while ((numData = in.read(buffer)) >= 0) {
				byteOut.write(buffer, 0, numData);
			}

			byte[] readByte = byteOut.toByteArray();
			Assert.assertArrayEquals(rawData, readByte);

		}

		// ---------------------------------
		// deletion
		// ---------------------------------
		for (int i = 0; i < numEntries; i++) {

			String fileName = "file" + decimalFormat.format(i);
			VFSPath filePath = homeDir.getChildPath(fileName);

			LOGGER.debug("Delete " + fileName);
			Assert.assertTrue(filePath.exists());
			VFSEntry fileEntry = filePath.createFile();
			fileEntry.delete();
			Assert.assertFalse(filePath.exists());
		}
	}

	private byte[] generateRandomData() {
		byte[] bytes = new byte[rnd.nextInt(10000)];
		rnd.nextBytes(bytes);
		return bytes;
	}
}
