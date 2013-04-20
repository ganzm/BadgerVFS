package ch.eth.jcd.badgers.vfs.test.core.interfaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.FindInFolderCallback;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public abstract class IVFSDiskManagerTest {

	public abstract VFSDiskManagerFactory getVFSDiskManagerFactory() throws VFSException;

	public abstract DiskConfiguration getConfiguration() throws VFSException;

	public abstract void setVFSDiskManager(VFSDiskManager manager) throws VFSException;

	protected VFSDiskManager diskManager;

	@Before
	public void beforeTest() throws VFSException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		DiskConfiguration config = getConfiguration();
		String filePath = config.getHostFilePath();
		VFSDiskManagerFactory factory = getVFSDiskManagerFactory();
		diskManager = factory.openDiskManager(config);
		setVFSDiskManager(diskManager);
		assertTrue("Expected File to exist", new File(filePath).exists());
	}

	@After
	public void afterTest() throws VFSException {
		diskManager.close();
	}

	@Test
	public void testCreateAndDispose() throws VFSException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		assertTrue("Expected File exist", new File(diskManager.getDiskConfiguration().getHostFilePath()).exists());

		diskManager.close();

		assertTrue("Expected File still exist", new File(diskManager.getDiskConfiguration().getHostFilePath()).exists());

		diskManager = getVFSDiskManagerFactory().openDiskManager(getConfiguration());
		assertTrue("Expected File to exist", new File(diskManager.getDiskConfiguration().getHostFilePath()).exists());
		diskManager.close();
	}

	@Test
	public void testGetRoot() throws VFSException {
		assertTrue("Expected File to exist", new File(diskManager.getDiskConfiguration().getHostFilePath()).exists());
		VFSEntry entry = diskManager.getRoot();
		assertTrue("Expected Root Entry to exist", entry.getPath().exists());
	}

	@Test
	public void testFind() throws VFSException, IOException {
		String dir1 = "FindDir";
		String findDir2 = "FindDir2";
		String dir2 = "SubFindDir";
		String dir3 = "SubNotFDir";
		String file1 = "FindFile1.txt";
		String file2 = "NoAvailFile1.txt";

		final List<VFSPath> results = new LinkedList<>();
		Set<String> expectedFileNames = new HashSet<>();
		expectedFileNames.add("FindDir");
		expectedFileNames.add("SubFindDir");
		expectedFileNames.add("FindDir2");
		expectedFileNames.add("FindFile1.txt");
		Set<String> expectedAbsoluteFilePaths = new HashSet<>();
		expectedAbsoluteFilePaths.add("/FindDir2");
		expectedAbsoluteFilePaths.add("/FindDir");
		expectedAbsoluteFilePaths.add("/FindDir/SubFindDir");
		expectedAbsoluteFilePaths.add("/FindDir/SubNotFDir/FindFile1.txt");

		VFSEntry rootEntry = diskManager.getRoot();
		VFSPath notFindDir2Path = rootEntry.getChildPath(findDir2);
		assertFalse("Expected directory notFindDir2 not exists", notFindDir2Path.exists());
		notFindDir2Path.createDirectory();
		assertTrue("Expected directory notFindDir2 exists", notFindDir2Path.exists());

		VFSPath dir1Path = rootEntry.getChildPath(dir1);
		assertFalse("Expected directory Dir1 not exists", dir1Path.exists());
		VFSEntry dir1Entry = dir1Path.createDirectory();
		assertTrue("Expected directory Dir1 exists", dir1Path.exists());

		VFSPath dir2Path = dir1Entry.getChildPath(dir2);
		assertFalse("Expected directory Dir2 not exists", dir2Path.exists());
		VFSEntry dir2Entry = dir2Path.createDirectory();
		assertTrue("Expected directory Dir2 exists", dir2Path.exists());

		VFSPath dir3Path = dir1Entry.getChildPath(dir3);
		assertFalse("Expected directory Dir3 not exists", dir3Path.exists());
		VFSEntry dir3Entry = dir3Path.createDirectory();
		assertTrue("Expected directory Dir3 exists", dir3Path.exists());

		VFSPath file1Path = dir3Entry.getChildPath(file1);
		assertFalse("Expected file File1.txt not exists", file1Path.exists());
		VFSEntry entry = file1Path.createFile();
		assertTrue("Expected file File1.txt exists", file1Path.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write("Test\n");
		}

		VFSPath file2Path = dir2Entry.getChildPath(file2);
		assertFalse("Expected file File2.txt not exists", file2Path.exists());
		VFSEntry file2entry = file2Path.createFile();
		assertTrue("Expected file File2.txt exists", file2Path.exists());
		try (OutputStream out = file2entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write("Test2\n");
		}

		diskManager.find("*Find*", new FindInFolderCallback() {

			@Override
			public boolean stopSearch(VFSPath currentDirectory) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void foundEntry(VFSPath path) {
				results.add(path);
			}
		});

		assertEquals("Expected 4 files found, but founded: " + results.size(), 4, results.size());

		assertTrue("Expected FileName(0) exists in founded items", expectedFileNames.contains(results.get(0).getName()));
		assertTrue("Expected FileName(1) exists in founded items", expectedFileNames.contains(results.get(1).getName()));
		assertTrue("Expected FileName(2) exists in founded items", expectedFileNames.contains(results.get(2).getName()));
		assertTrue("Expected FileName(3) exists in founded items", expectedFileNames.contains(results.get(3).getName()));

		assertTrue("Expected AbsoluteFilePaths(0) exists in founded items", expectedAbsoluteFilePaths.contains(results.get(0).getAbsolutePath()));
		assertTrue("Expected AbsoluteFilePaths(1) exists in founded items", expectedAbsoluteFilePaths.contains(results.get(1).getAbsolutePath()));
		assertTrue("Expected AbsoluteFilePaths(2) exists in founded items", expectedAbsoluteFilePaths.contains(results.get(2).getAbsolutePath()));
		assertTrue("Expected AbsoluteFilePaths(3) exists in founded items", expectedAbsoluteFilePaths.contains(results.get(3).getAbsolutePath()));

		assertTrue("Expected free space > 0", diskManager.getFreeSpace() > 0);

	}

	@Test
	public void testCreatePath() throws VFSException {
		String createPathDir = "createPath";
		String createPathFile = "createPath.txt";

		VFSEntry rootEntry = diskManager.getRoot();

		VFSPath createPathDirPath = rootEntry.getChildPath(createPathDir);
		assertFalse("Expected directory IsDirectory not exists", createPathDirPath.exists());
		VFSEntry createPathDirEntry = createPathDirPath.createDirectory();
		assertTrue("Expected directory IsDirectory exists", createPathDirPath.exists());
		assertTrue("Expected isDirectory is true", createPathDirEntry.isDirectory());

		VFSPath createPathFilePath = diskManager.createPath("/" + createPathDir + "/" + createPathFile);
		assertFalse("Expected file IsNotADirectory.txt not exists", createPathFilePath.exists());
		createPathFilePath.createFile();
		assertTrue("Expected file IsNotADirectory.txt exists", createPathFilePath.exists());
		assertEquals("Expected absolute path is /" + createPathDir + "/" + createPathFile, "/" + createPathDir + "/" + createPathFile,
				createPathFilePath.getAbsolutePath());

	}
}
