package ch.eth.jcd.badgers.vfs.test.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.mock.MockedVFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.mock.MockedVFSEntry;
import ch.eth.jcd.badgers.vfs.mock.MockedVFSPath;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;

public class MockedVFSDiskManagerImplTest {

	@BeforeClass
	public static void beforeClass() {
		UnittestLogger.init();
	}

	private String fileName;

	@Before
	public void setup() {

		String tempDir = System.getProperty("java.io.tmpdir");

		if (tempDir.endsWith("/") || tempDir.endsWith("\\")) {
			fileName = tempDir + "mocked";
		} else {
			fileName = tempDir + File.separatorChar + "mocked";
		}

		if (new File(fileName).exists() && new File(fileName).isDirectory()) {
			Path dir = Paths.get(fileName);
			try {
				Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

						System.out.println("Deleting dir: " + dir);
						if (exc == null) {
							Files.delete(dir);
							return FileVisitResult.CONTINUE;
						} else {
							throw exc;
						}
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

						System.out.println("Deleting file: " + file);
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}

				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Test
	public void testCreateAndDispose() throws VFSException {
		DiskConfiguration config = new DiskConfiguration();
		config.setHostFilePath(fileName);

		MockedVFSDiskManagerImpl instance = MockedVFSDiskManagerImpl.create(config);

		Assert.assertTrue("Expected File to exist", new File(fileName).exists());

		instance.close();

		Assert.assertTrue("Expected File to exist", new File(fileName).exists());

		instance = MockedVFSDiskManagerImpl.open(config);

		Assert.assertTrue("Expected File to exist", new File(fileName).exists());

		instance.dispose();

		Assert.assertFalse("Expected File to be deleted", new File(fileName).exists());
	}

	@Test
	public void testgetRoot() throws VFSException {
		DiskConfiguration config = new DiskConfiguration();
		config.setHostFilePath(fileName);

		MockedVFSDiskManagerImpl instance = MockedVFSDiskManagerImpl.create(config);

		Assert.assertTrue("Expected File to exist", new File(fileName).exists());

		MockedVFSEntry entry = (MockedVFSEntry) instance.getRoot();
		Assert.assertEquals(((MockedVFSPath) entry.getPath()).getAbsolutPath(), fileName);

		instance.dispose();

		Assert.assertFalse("Expected File to be deleted", new File(fileName).exists());
	}

}
