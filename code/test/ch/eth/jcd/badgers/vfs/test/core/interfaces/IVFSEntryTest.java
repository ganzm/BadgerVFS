package ch.eth.jcd.badgers.vfs.test.core.interfaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.FindInFolderCallback;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public abstract class IVFSEntryTest {

	private static final Logger LOGGER = Logger.getLogger(IVFSEntryTest.class);

	public abstract VFSDiskManager getVFSDiskManager() throws VFSException;

	public abstract void setVFSDiskManager(VFSDiskManager manager) throws VFSException;

	@Before
	public void beforeTest() throws VFSException {
		Class<? extends VFSDiskManager> class1;
		try {
			class1 = Class.forName(getVFSDiskManager().getClass().getName()).asSubclass(VFSDiskManager.class);
			Method methodOpen = class1.getMethod("open", DiskConfiguration.class);
			DiskConfiguration config = getVFSDiskManager().getDiskConfiguration();
			String filePath = getVFSDiskManager().getDiskConfiguration().getHostFilePath();
			setVFSDiskManager(null);
			VFSDiskManager diskManager = (VFSDiskManager) methodOpen.invoke(null, config);
			setVFSDiskManager(diskManager);
			assertTrue("Expected File to exist", new File(filePath).exists());
		} catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new VFSException(e);
		}
	}

	@After
	public void afterTest() throws VFSException {
		getVFSDiskManager().close();
	}

	@Test
	public void testIsDirectory() throws VFSException {
		String isDirectory = "IsDirectory";
		String isNotADirectory = "IsNotADirectory.txt";

		VFSEntry rootEntry = getVFSDiskManager().getRoot();

		VFSPath isDirectoryPath = rootEntry.getChildPath(isDirectory);
		assertFalse("Expected directory IsDirectory not exists", isDirectoryPath.exists());
		VFSEntry directory = isDirectoryPath.createDirectory();
		assertTrue("Expected directory IsDirectory exists", isDirectoryPath.exists());
		assertTrue("Expected isDirectory is true", directory.isDirectory());

		VFSPath aFileNotDirectory = directory.getChildPath(isNotADirectory);
		assertFalse("Expected file IsNotADirectory.txt not exists", aFileNotDirectory.exists());
		aFileNotDirectory.createFile();
		assertTrue("Expected file IsNotADirectory.txt exists", aFileNotDirectory.exists());
	}

	@Test
	public void testGetOutputStreamTest() throws VFSException {
		VFSEntry rootEntry = getVFSDiskManager().getRoot();
		VFSPath newFile = rootEntry.getChildPath("newOutputStreamTest.txt");
		assertFalse("Expected file newOutputStreamTest.txt not exists", newFile.exists());
		VFSEntry entry = newFile.createFile();
		assertTrue("Expected file newOutputStreamTest.txt exists", newFile.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write("newOutputStreamTest String");
		} catch (IOException e) {
			throw new VFSException(e);
		}
		assertTrue("Expected file newOutputStreamTest.txt still exists", newFile.exists());
	}

	@Test
	public void testGetInputStreamTest() throws VFSException {
		String fileName = "newInputStreamTest.txt";
		String fileContent = "newInputStreamTest String";
		VFSEntry rootEntry = getVFSDiskManager().getRoot();
		VFSPath newFile = rootEntry.getChildPath(fileName);
		assertFalse("Expected file newInputStreamTest.txt not exists", newFile.exists());
		VFSEntry entry = newFile.createFile();
		assertTrue("Expected file newInputStreamTest.txt exists", newFile.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write(fileContent);
		} catch (IOException e) {
			throw new VFSException(e);
		}
		assertTrue("Expected file newInputStreamTest.txt still exists", newFile.exists());
		String readed = null;
		try (InputStream in = entry.getInputStream(); InputStreamReader reader = new InputStreamReader(in); BufferedReader br = new BufferedReader(reader)) {
			readed = br.readLine();
		} catch (IOException e) {
			throw new VFSException(e);
		}
		assertEquals("Expected readed content in testGetInputStreamTest: " + fileContent, fileContent, readed);
	}

	@Test
	public void testRenameTo() throws VFSException {
		String fileNameBefore = "beforeRename.txt";
		String fileNameAfter = "afterRename.txt";
		String fileContent = "Test String";
		VFSEntry rootEntry = getVFSDiskManager().getRoot();
		VFSPath newFile = rootEntry.getChildPath(fileNameBefore);
		assertFalse("Expected file beforeRename.txt not exists", newFile.exists());
		VFSEntry entry = newFile.createFile();
		assertTrue("Expected file beforeRename.txt exists", newFile.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write(fileContent);
		} catch (IOException e) {
			throw new VFSException(e);
		}
		assertTrue("Expected file beforeRename.txt still exists", newFile.exists());

		entry.renameTo(fileNameAfter);

		assertFalse("Expected file beforeRename.txt not exists anymore", newFile.exists());

		VFSPath afterRenamePath = rootEntry.getChildPath(fileNameAfter);
		assertTrue("Expected file afterRename.txt exists", afterRenamePath.exists());

		String readed = null;
		try (InputStream in = entry.getInputStream(); InputStreamReader reader = new InputStreamReader(in); BufferedReader br = new BufferedReader(reader)) {
			readed = br.readLine();
		} catch (IOException e) {
			throw new VFSException(e);
		}
		assertEquals("Expected readed content from testRenameTo: " + fileContent, fileContent, readed);

	}

	@Test
	public void testCopyTo() throws VFSException {
		String copyFromFolder = "CopyFrom";
		String copyToFolder = "CopyTo";
		String copyFolder = "Copy";
		String copyFile = "copy.txt";
		String fileContent = "copy file";

		VFSEntry rootEntry = getVFSDiskManager().getRoot();
		VFSPath copyFromPath = rootEntry.getChildPath(copyFromFolder);
		assertFalse("Expected direcotry CopyFrom not exists", copyFromPath.exists());
		VFSEntry copyFromEntry = copyFromPath.createDirectory();
		assertTrue("Expected direcotry CopyFrom exists", copyFromPath.exists());

		VFSPath copyToPath = rootEntry.getChildPath(copyToFolder);
		assertFalse("Expected direcotry CopyTo not exists", copyToPath.exists());

		VFSPath copyPath = copyFromEntry.getChildPath(copyFolder);
		assertFalse("Expected direcotry Copy not exists", copyPath.exists());
		VFSEntry copyEntry = copyPath.createDirectory();
		assertTrue("Expected direcotry Copy exists", copyPath.exists());

		VFSPath newFile = copyEntry.getChildPath(copyFile);
		VFSEntry entry = newFile.createFile();
		assertTrue("Expected file copy.txt exists", newFile.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write(fileContent);
		} catch (IOException e) {
			throw new VFSException(e);
		}
		assertTrue("Expected file copy.txt still exists", newFile.exists());

		copyFromEntry.copyTo(copyToPath);
		assertTrue("Expected directory CopyTo still exists", copyToPath.exists());
		assertTrue("Expected directory Copy still exists", copyToPath.getVFSEntry().getChildPath(copyFolder).exists());
		assertTrue("Expected file copy.txt exists", copyToPath.getVFSEntry().getChildPath(copyFolder).getVFSEntry().getChildPath(copyFile).exists());
		assertTrue("Expected directory Copy still exists", copyFromEntry.getChildPath(copyFolder).exists());
		assertTrue("Expected file copy.txt still exists", copyFromPath.getVFSEntry().getChildPath(copyFolder).getVFSEntry().getChildPath(copyFile).exists());
		entry = copyToPath.getVFSEntry().getChildPath(copyFolder).getVFSEntry().getChildPath(copyFile).getVFSEntry();
		String readed = null;
		try (InputStream in = entry.getInputStream(); InputStreamReader reader = new InputStreamReader(in); BufferedReader br = new BufferedReader(reader)) {
			readed = br.readLine();
		} catch (IOException e) {
			throw new VFSException(e);
		}
		assertEquals("Expected readed content: " + fileContent, fileContent, readed);
	}

	@Test
	public void testMoveTo() throws VFSException {
		String moveFromFolder = "MoveFrom";
		String moveToFolder = "MoveTo";
		String moveFolder = "Move";
		String moveFile = "move.txt";
		String fileContent = "move file";

		VFSEntry rootEntry = getVFSDiskManager().getRoot();
		VFSPath moveFromPath = rootEntry.getChildPath(moveFromFolder);
		assertFalse("Expected direcotry MoveFrom not exists", moveFromPath.exists());
		VFSEntry moveFromEntry = moveFromPath.createDirectory();
		assertTrue("Expected direcotry MoveFrom exists", moveFromPath.exists());

		VFSPath moveToPath = rootEntry.getChildPath(moveToFolder);
		assertFalse("Expected direcotry MoveTo not exists", moveToPath.exists());

		VFSPath movePath = moveFromEntry.getChildPath(moveFolder);
		assertFalse("Expected direcotry Move not exists", movePath.exists());
		VFSEntry moveEntry = movePath.createDirectory();
		assertTrue("Expected direcotry Move exists", movePath.exists());

		VFSPath newFile = moveEntry.getChildPath(moveFile);
		VFSEntry entry = newFile.createFile();
		assertTrue("Expected file move.txt exists", newFile.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write(fileContent);
		} catch (IOException e) {
			throw new VFSException(e);
		}
		assertTrue("Expected file move.txt exists", newFile.exists());

		moveFromEntry.moveTo(moveToPath);
		assertTrue("Expected directory MoveTo exists", moveToPath.exists());
		assertTrue("Expected directory Move exists", moveToPath.getVFSEntry().getChildPath(moveFolder).exists());
		assertTrue("Expected file move.txt exists", moveToPath.getVFSEntry().getChildPath(moveFolder).getVFSEntry().getChildPath(moveFile).exists());
		assertFalse("Expected directory MoveFrom not exists", moveFromEntry.getChildPath(moveFolder).exists());
		assertFalse("Expected file move.txt not exists", moveFromPath.getVFSEntry().getChildPath(moveFolder).getVFSEntry().getChildPath(moveFile).exists());
		entry = moveToPath.getVFSEntry().getChildPath(moveFolder).getVFSEntry().getChildPath(moveFile).getVFSEntry();
		String readed = null;
		try (InputStream in = entry.getInputStream(); InputStreamReader reader = new InputStreamReader(in); BufferedReader br = new BufferedReader(reader)) {
			readed = br.readLine();
		} catch (IOException e) {
			throw new VFSException(e);
		}
		assertEquals("Expected readed content: " + fileContent, fileContent, readed);
	}

	@Test
	public void testGetChildren() throws VFSException {
		getVFSDiskManager().dispose();
		Class<? extends VFSDiskManager> class1;
		try {
			class1 = Class.forName(getVFSDiskManager().getClass().getName()).asSubclass(VFSDiskManager.class);

			DiskConfiguration config = getVFSDiskManager().getDiskConfiguration();
			Method methodCreate = class1.getMethod("create", DiskConfiguration.class);
			VFSDiskManager createdDiskManager = (VFSDiskManager) methodCreate.invoke(null, config);
			createdDiskManager.close();

			Method methodOpen = class1.getMethod("open", DiskConfiguration.class);
			VFSDiskManager openedDiskManager = (VFSDiskManager) methodOpen.invoke(null, config);

			setVFSDiskManager(openedDiskManager);

			assertTrue("Expected File to exist", new File(getVFSDiskManager().getDiskConfiguration().getHostFilePath()).exists());
		} catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new VFSException(e);
		}

		String dir1 = "Dir1";
		String dir2 = "Dir2";
		String file1 = "File1.txt";

		VFSEntry rootEntry = getVFSDiskManager().getRoot();
		VFSPath dir1Path = rootEntry.getChildPath(dir1);
		assertFalse("Expected directory Dir1 not exists", dir1Path.exists());
		dir1Path.createDirectory();
		assertTrue("Expected directory Dir1 exists", dir1Path.exists());

		VFSPath dir2Path = rootEntry.getChildPath(dir2);
		assertFalse("Expected directory Dir2 not exists", dir2Path.exists());
		dir2Path.createDirectory();
		assertTrue("Expected directory Dir2 exists", dir2Path.exists());

		VFSPath file1Path = rootEntry.getChildPath(file1);
		assertFalse("Expected file File1.txt not exists", file1Path.exists());
		VFSEntry entry = file1Path.createFile();
		assertTrue("Expected file File1.txt exists", file1Path.exists());
		try (OutputStream out = entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write("Test\n");
		} catch (IOException e) {
			throw new VFSException(e);
		}

		List<VFSEntry> childs = getVFSDiskManager().getRoot().getChildren();
		assertTrue("Expected root has 3 children", childs.size() == 3);
		for (VFSEntry child : childs) {
			LOGGER.info(child);
		}

	}

	@Test
	public void testDelete() throws VFSException {
		String delDir = "delDir";
		String delFile = "delFile.txt";

		VFSEntry rootEntry = getVFSDiskManager().getRoot();

		VFSPath delDirectoryPath = rootEntry.getChildPath(delDir);
		assertFalse("Expected directory not exists", delDirectoryPath.exists());
		VFSEntry delDirectoryEntry = delDirectoryPath.createDirectory();
		assertTrue("Expected directory exists", delDirectoryPath.exists());
		assertTrue("Expected isDirectory is true", delDirectoryEntry.isDirectory());

		VFSPath delFilePath = delDirectoryEntry.getChildPath(delFile);
		assertFalse("Expected file not exists", delFilePath.exists());
		VFSEntry delFileEntry = delFilePath.createFile();
		assertTrue("Expected file exists", delFilePath.exists());
		delFileEntry.delete();
		assertFalse("Expected file not exists", delFilePath.exists());
		delDirectoryEntry.delete();
		assertFalse("Expected directory not exists", delDirectoryPath.exists());
	}

	@Test
	public void testGetParent() throws VFSException {
		String testGetParent = "testGetParent";
		VFSEntry rootEntry = getVFSDiskManager().getRoot();

		VFSPath testGetParentPath = rootEntry.getChildPath(testGetParent);
		assertFalse("Expected directory testGetParentnot exists", testGetParentPath.exists());
		VFSEntry testGetParentEntry = testGetParentPath.createDirectory();
		assertTrue("Expected directory testGetParent exists", testGetParentPath.exists());
		assertTrue("Expected isDirectory on testGetParent is true", testGetParentEntry.isDirectory());
		assertEquals(rootEntry.getPath().getAbsolutePath(), testGetParentEntry.getParent().getPath().getAbsolutePath());

	}

	@Test
	public void testFindInDirectory() throws VFSException {
		String dir1 = "FindDir";
		String notFindDir2 = "notFindDir2";
		String dir2 = "SubFindDir";
		String dir3 = "SubNotFDir";
		String file1 = "FindFile1.txt";
		String file2 = "NoAvailFile1.txt";

		final List<VFSPath> results = new LinkedList<>();
		Set<String> expectedFileNames = new HashSet<>();
		expectedFileNames.add("SubFindDir");
		expectedFileNames.add("FindFile1.txt");
		Set<String> expectedAbsoluteFilePaths = new HashSet<>();
		expectedAbsoluteFilePaths.add("/FindDir/SubFindDir");
		expectedAbsoluteFilePaths.add("/FindDir/SubNotFDir/FindFile1.txt");

		VFSEntry rootEntry = getVFSDiskManager().getRoot();
		VFSPath notFindDir2Path = rootEntry.getChildPath(notFindDir2);
		assertFalse("Expected directory notFindDir2 not exists", notFindDir2Path.exists());
		VFSEntry notFindDir2Entry = notFindDir2Path.createDirectory();
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
		} catch (IOException e) {
			throw new VFSException(e);
		}

		VFSPath file2Path = dir2Entry.getChildPath(file2);
		assertFalse("Expected file File2.txt not exists", file2Path.exists());
		VFSEntry file2entry = file2Path.createFile();
		assertTrue("Expected file File2.txt exists", file2Path.exists());
		try (OutputStream out = file2entry.getOutputStream(0);
				OutputStreamWriter writer = new OutputStreamWriter(out);
				BufferedWriter br = new BufferedWriter(writer)) {
			br.write("Test2\n");
		} catch (IOException e) {
			throw new VFSException(e);
		}

		dir1Entry.findInFolder("Find", new FindInFolderCallback() {

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

		assertTrue("Expected 2 files found, but founded: " + results.size(), results.size() == 2);

		assertTrue("Expected FileName(0) exists in founded items", expectedFileNames.contains(results.get(0).getName()));
		assertTrue("Expected FileName(1) exists in founded items", expectedFileNames.contains(results.get(1).getName()));

		assertTrue("Expected AbsoluteFilePaths(0) exists in founded items", expectedAbsoluteFilePaths.contains(results.get(0).getAbsolutePath()));
		assertTrue("Expected AbsoluteFilePaths(1) exists in founded items", expectedAbsoluteFilePaths.contains(results.get(1).getAbsolutePath()));

	}
}
