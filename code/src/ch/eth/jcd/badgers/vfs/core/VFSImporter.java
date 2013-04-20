package ch.eth.jcd.badgers.vfs.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.exception.VFSInvalidPathException;
import ch.eth.jcd.badgers.vfs.util.ChannelUtil;

public class VFSImporter {

	private static final Logger LOGGER = Logger.getLogger(VFSImporter.class);

	private int totalEntries = 0;
	private int entriesDone = 0;

	/**
	 * 
	 * @param importFile
	 *            e.g. c:\temp\folderToImport
	 * @param path
	 *            e.g. /home/imported
	 * @throws VFSException
	 */
	public void importFileOrFolder(String pathToImportFile, VFSPath path) throws VFSInvalidPathException, VFSException {
		totalEntries = 1;
		entriesDone = 0;

		File importFile = new File(pathToImportFile);
		if (!importFile.exists()) {
			throw new VFSInvalidPathException("Path on host file system does not exist" + pathToImportFile);
		}

		try {
			if (path.exists()) {
				throw new VFSInvalidPathException("Cant import to " + path.getAbsolutePath() + " VFSPath already exists " + path);
			}

			if (importFile.isDirectory()) {
				importFolder(importFile, path);

			} else {
				importFile(importFile, path);
			}
		} catch (IOException e) {
			throw new VFSException(e);
		}
	}

	private void importFolder(File folderToImport, VFSPath targetFolderPath) throws VFSException, IOException {
		LOGGER.debug("Import Folder " + folderToImport.getAbsolutePath());
		VFSEntry newFolder = targetFolderPath.createDirectory();

		File[] children = folderToImport.listFiles();
		totalEntries += children.length;
		for (File child : children) {
			VFSPath childPath = newFolder.getChildPath(child.getName());
			if (child.isDirectory()) {
				importFolder(child, childPath);
			} else {
				importFile(child, childPath);
			}
		}

		++entriesDone;
	}

	private void importFile(File fileToImport, VFSPath targetFilePath) throws IOException, VFSException {
		LOGGER.debug("Import File " + fileToImport.getAbsolutePath());
		VFSEntry newFile = targetFilePath.createFile();

		try {
			FileInputStream fis = new FileInputStream(fileToImport);
			OutputStream os = newFile.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE);
			ChannelUtil.fastStreamCopy(fis, os);

			++entriesDone;
		} catch (IOException | VFSException e) {
			try {
				LOGGER.debug("deleting partially created File at " + targetFilePath.getAbsolutePath());
				newFile.delete();
			} catch (VFSException ex) {
				LOGGER.error("internal error while deleting partially created file", ex);
			}

			// cleanup done, now rethrow the exception
			throw e;
		}
	}

	/**
	 * Progress indicator method
	 * 
	 * @return
	 */
	public int getTotalEntries() {
		return totalEntries;
	}

	/**
	 * Progress indicator method
	 * 
	 * @return
	 */
	public int getEntriesDone() {
		return entriesDone;
	}
}
