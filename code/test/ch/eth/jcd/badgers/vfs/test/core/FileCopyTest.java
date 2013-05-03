package ch.eth.jcd.badgers.vfs.test.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.test.VFSDiskManagerTestBase;
import ch.eth.jcd.badgers.vfs.test.testutil.CoreTestUtil;

public class FileCopyTest extends VFSDiskManagerTestBase {

	private final Random rnd = new Random(0);

	@Test
	public void testCreateSimpleFiles() throws VFSException, IOException {

		VFSEntry rootEntry = diskManager.getRoot();

		// create home folder
		String folderName = "home";
		VFSPath path = rootEntry.getChildPath(folderName);
		VFSEntry homeDir = path.createDirectory();

		// create TempFile
		VFSPath tempFilePath = homeDir.getChildPath("tempFile.txt");
		VFSEntry tempFile = tempFilePath.createFile();
		byte[] tempFileContent = generateRandomData();
		try (OutputStream out = tempFile.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
			out.write(tempFileContent);
		}

		// copy temp File
		VFSPath copiedFilePath = homeDir.getChildPath("copied.txt");
		tempFile.copyTo(copiedFilePath);
		Assert.assertTrue(copiedFilePath.exists());
		CoreTestUtil.assertFileContentEquals(tempFile, copiedFilePath.getVFSEntry());

		// override temp file
		try (OutputStream out = tempFile.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
			out.write("Hallo Welt".getBytes());
		}

		VFSEntry copiedFile = copiedFilePath.getVFSEntry();

		byte[] actualByteData = CoreTestUtil.fileToBytes(copiedFile);
		Assert.assertArrayEquals(tempFileContent, actualByteData);
	}

	private byte[] generateRandomData() {
		byte[] bytes = new byte[rnd.nextInt(10000)];
		rnd.nextBytes(bytes);
		return bytes;
	}
}
