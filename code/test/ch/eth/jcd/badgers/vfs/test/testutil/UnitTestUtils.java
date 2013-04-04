package ch.eth.jcd.badgers.vfs.test.testutil;

import java.io.File;

import org.junit.Assert;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;

public class UnitTestUtils {

	public static DiskConfiguration getMockedConfig(String rootFolderName) {
		DiskConfiguration config = new DiskConfiguration();
		config.setHostFilePath(getRootDir(rootFolderName));
		return config;

	}

	private static String getRootDir(String rootFolderName) {
		String tempDir = System.getProperty("java.io.tmpdir");
		String fileName;
		if (tempDir.endsWith("/") || tempDir.endsWith("\\")) {
			fileName = tempDir + rootFolderName;
		} else {
			fileName = tempDir + File.separatorChar + rootFolderName;
		}
		return fileName;

	}

	public static void deleteFileIfExist(String filePath) {
		File oldFile = new File(filePath);
		if (oldFile.exists()) {
			Assert.assertTrue("Could not delete file " + oldFile.getName(), oldFile.delete());
		}
	}

}
