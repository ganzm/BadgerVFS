package ch.eth.jcd.badgers.vfs.test.core.interfaces;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.FindInFolderCallback;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public abstract class IVFSDiskManagerTest {

	public abstract VFSDiskManager getVFSDiskManager() throws VFSException;

	@Test
	public void testCreateAndDispose() throws VFSException {
		assertTrue("Expected File exist", new File(getVFSDiskManager().getDiskConfiguration().getHostFilePath()).exists());

		getVFSDiskManager().close();

		assertTrue("Expected File still exist", new File(getVFSDiskManager().getDiskConfiguration().getHostFilePath()).exists());

		Class<? extends VFSDiskManager> class1;
		try {
			class1 = Class.forName(getVFSDiskManager().getClass().getName()).asSubclass(VFSDiskManager.class);
			Method method = class1.getMethod("open", DiskConfiguration.class);
			VFSDiskManager diskManager = (VFSDiskManager) method.invoke(null, getVFSDiskManager().getDiskConfiguration());
			assertTrue("Expected File to exist", new File(getVFSDiskManager().getDiskConfiguration().getHostFilePath()).exists());
			diskManager.close();
		} catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new VFSException(e);
		}
	}

	@Test
	public void testGetRoot() throws VFSException {
		assertTrue("Expected File to exist", new File(getVFSDiskManager().getDiskConfiguration().getHostFilePath()).exists());
		VFSEntry entry = getVFSDiskManager().getRoot();
		assertTrue("Expected Root Entry to exist", entry.getPath().exists());
	}

	@Test
	public void testFind() throws VFSException {
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

		VFSEntry rootEntry = getVFSDiskManager().getRoot();
		VFSPath notFindDir2Path = rootEntry.getChildPath(findDir2);
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

		getVFSDiskManager().find("Find", new FindInFolderCallback() {

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

		assertTrue("Expected 4 files found, but founded: " + results.size(), results.size() == 4);

		assertTrue("Expected FileName(0) exists in founded items", expectedFileNames.contains(results.get(0).getName()));
		assertTrue("Expected FileName(1) exists in founded items", expectedFileNames.contains(results.get(1).getName()));
		assertTrue("Expected FileName(2) exists in founded items", expectedFileNames.contains(results.get(2).getName()));
		assertTrue("Expected FileName(3) exists in founded items", expectedFileNames.contains(results.get(3).getName()));

		assertTrue("Expected AbsoluteFilePaths(0) exists in founded items", expectedAbsoluteFilePaths.contains(results.get(0).getAbsolutePath()));
		assertTrue("Expected AbsoluteFilePaths(1) exists in founded items", expectedAbsoluteFilePaths.contains(results.get(1).getAbsolutePath()));
		assertTrue("Expected AbsoluteFilePaths(2) exists in founded items", expectedAbsoluteFilePaths.contains(results.get(2).getAbsolutePath()));
		assertTrue("Expected AbsoluteFilePaths(3) exists in founded items", expectedAbsoluteFilePaths.contains(results.get(3).getAbsolutePath()));

	}
}
