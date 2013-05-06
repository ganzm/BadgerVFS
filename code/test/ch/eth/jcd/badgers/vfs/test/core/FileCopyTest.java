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

	@Test
	public void testMultiCopy() throws VFSException, IOException {
		byte[] f1Content = generateRandomData();
		byte[] f2Content = generateRandomData();
		byte[] f3Content = generateRandomData();
		byte[] f4Content = generateRandomData();

		VFSEntry rootEntry = diskManager.getRoot();

		VFSPath multiCopyPath = rootEntry.getChildPath("multicopy");
		VFSEntry multiCopyDir = multiCopyPath.createDirectory();

		VFSPath f1path = multiCopyDir.getChildPath("1");
		VFSPath f2Path = multiCopyDir.getChildPath("2");
		VFSPath f3Path = multiCopyDir.getChildPath("3");
		VFSPath f4Path = multiCopyDir.getChildPath("4");
		VFSPath f5Path = multiCopyDir.getChildPath("5");

		VFSEntry f1 = f1path.createFile();
		try (OutputStream out = f1.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
			out.write(f1Content);
		}

		f1.copyTo(f2Path);
		VFSEntry f2 = f2Path.getVFSEntry();
		try (OutputStream out = f2.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
			out.write(f2Content);
		}

		f1.copyTo(f3Path);
		VFSEntry f3 = f3Path.getVFSEntry();
		try (OutputStream out = f3.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
			out.write(f3Content);
		}

		f1.copyTo(f4Path);
		VFSEntry f4 = f3Path.getVFSEntry();
		try (OutputStream out = f4.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
			out.write(f4Content);
		}

		f1.copyTo(f5Path);

		Assert.assertArrayEquals(f1Content, CoreTestUtil.fileToBytes(f1));
		Assert.assertArrayEquals(f2Content, CoreTestUtil.fileToBytes(f2));
		Assert.assertArrayEquals(f3Content, CoreTestUtil.fileToBytes(f3));
		Assert.assertArrayEquals(f4Content, CoreTestUtil.fileToBytes(f4));
		Assert.assertArrayEquals(f1Content, CoreTestUtil.fileToBytes(f1));

	}

	private byte[] generateRandomData() {
		byte[] bytes = new byte[rnd.nextInt(10000)];
		rnd.nextBytes(bytes);
		return bytes;
	}
}
