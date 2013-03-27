/**
 * JCD Virtual File System 
 * spring 2013
 * Group: Badgers
 */
package ch.eth.jcd.badgers.vfs.core;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
import ch.eth.jcd.badgers.vfs.core.data.DataSectionHandler;
import ch.eth.jcd.badgers.vfs.core.directory.DirectoryBlock;
import ch.eth.jcd.badgers.vfs.core.directory.DirectorySectionHandler;
import ch.eth.jcd.badgers.vfs.core.header.HeaderSectionHandler;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

/**
 * class VirtualDiskManager
 * 
 * handles a single virtual disk file
 * 
 */
public class VFSDiskManagerImpl implements VFSDiskManager {

	private static Logger logger = Logger.getLogger(VFSDiskManagerImpl.class);

	private final DiskConfiguration config;

	/** the file we write our data to */
	private RandomAccessFile virtualDiskFile;

	/**
	 * Root folder to the file system
	 */
	private VFSEntry root;

	private HeaderSectionHandler headerSectionHandler;

	private DirectorySectionHandler directorySectionHandler;

	private DataSectionHandler dataSectionHandler;

	/**
	 * Private constructor
	 */
	private VFSDiskManagerImpl(DiskConfiguration config) {
		this.config = config;
	}

	/**
	 * creates a new virtual disk
	 * 
	 * @param config
	 *            Configuration used
	 * @return
	 */
	public static VFSDiskManagerImpl create(DiskConfiguration config) throws VFSException {
		try {
			logger.info("Create new BadgerVFS Disk on " + config.getHostFilePath());
			logger.debug("Using Config " + config.toString());
			VFSDiskManagerImpl mgr = new VFSDiskManagerImpl(config);

			File file = new File(config.getHostFilePath());
			if (file.exists()) {
				throw new VFSException("Cannot create VFSDiskManager because the file already exists " + config.getHostFilePath());
			}

			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

			long headerSectionOffset = 0;

			mgr.headerSectionHandler = HeaderSectionHandler.createNew(randomAccessFile, config, headerSectionOffset);
			long directorySectionOffset = mgr.headerSectionHandler.getSectionSize();
			long dataSectionOffset = mgr.headerSectionHandler.getDataSectionOffset();

			mgr.directorySectionHandler = DirectorySectionHandler.createNew(randomAccessFile, config, directorySectionOffset, dataSectionOffset);
			mgr.dataSectionHandler = DataSectionHandler.createNew(randomAccessFile, config, dataSectionOffset);

			mgr.virtualDiskFile = randomAccessFile;

			mgr.createRootFolder();

			return mgr;

		} catch (Exception e) {
			throw new VFSException(e);
		}
	}

	private void createRootFolder() throws IOException {
		logger.debug("Creating root folder...");

		DataBlock rootDirectoryDataBlock = dataSectionHandler.allocateNewDataBlock(virtualDiskFile, true);
		DirectoryBlock rootDirectoryBlock = directorySectionHandler.allocateNewDirectoryBlock(virtualDiskFile);

		prepareRootFolder(rootDirectoryDataBlock, rootDirectoryBlock);

		logger.debug("Creating root folder done");
	}

	private void openRootFolder() throws IOException {
		logger.debug("Opening root folder...");

		DataBlock rootDirectoryDataBlock = dataSectionHandler.loadDataBlock(virtualDiskFile, dataSectionHandler.getSectionOffset());
		DirectoryBlock rootDirectoryBlock = directorySectionHandler.loadDataBlock(virtualDiskFile, directorySectionHandler.getSectionOffset());

		prepareRootFolder(rootDirectoryDataBlock, rootDirectoryBlock);

		logger.debug("Opening root folder done");
	}

	private void prepareRootFolder(DataBlock rootDirectoryDataBlock, DirectoryBlock rootDirectoryBlock) {
		VFSPathImpl rootPath = new VFSPathImpl(this, "/");
		VFSDirectoryImpl rootDirectory = new VFSDirectoryImpl(this, rootPath);

		rootDirectory.setDataBlock(rootDirectoryDataBlock);
		rootDirectory.setDirectoryBlock(rootDirectoryBlock);

		this.root = rootDirectory;
	}

	/**
	 * Opens an existing virtual disk and opens is
	 * 
	 * @param config
	 *            Configuration used
	 * @return
	 */
	public static VFSDiskManagerImpl open(DiskConfiguration config) throws VFSException {

		try {
			logger.info("Open BadgerVFS Disk on " + config.getHostFilePath());
			logger.debug("Using Config " + config.toString());
			VFSDiskManagerImpl mgr = new VFSDiskManagerImpl(config);

			File file = new File(config.getHostFilePath());
			if (file.exists() == false) {
				throw new VFSException("Cannot open VFSDiskManager because the file does not exist " + config.getHostFilePath());
			}

			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

			long headerSectionOffset = 0;

			mgr.headerSectionHandler = HeaderSectionHandler.createExisting(randomAccessFile, config, headerSectionOffset);
			long directorySectionOffset = mgr.headerSectionHandler.getSectionSize();
			long dataSectionOffset = mgr.headerSectionHandler.getDataSectionOffset();

			mgr.directorySectionHandler = DirectorySectionHandler.createExisting(randomAccessFile, config, directorySectionOffset, dataSectionOffset);
			mgr.dataSectionHandler = DataSectionHandler.createExisting(randomAccessFile, config, dataSectionOffset);

			mgr.virtualDiskFile = randomAccessFile;

			return mgr;

		} catch (Exception e) {
			throw new VFSException(e);
		}
	}

	@Override
	public void close() throws VFSException {

		try {
			headerSectionHandler.close();
			directorySectionHandler.close();
			dataSectionHandler.close();

			virtualDiskFile.close();
		} catch (Exception ex) {
			throw new VFSException(ex);
		}
	}

	@Override
	public void dispose() throws VFSException {
		logger.info("Getting rid of " + config.getHostFilePath());
		close();

		if (new File(config.getHostFilePath()).delete() == false) {
			throw new VFSException("Could not delete File " + config.getHostFilePath());
		}
	}

	@Override
	public long getFreeSpace() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public VFSEntry getRoot() {
		return root;
	}

	@Override
	public VFSPath CreatePath(String pathString) throws VFSException {
		VFSPathImpl path = new VFSPathImpl(this, pathString);
		return path;
	}

	public HeaderSectionHandler getHeaderSectionHandler() {
		return headerSectionHandler;
	}

	public DirectorySectionHandler getDirectorySectionHandler() {
		return directorySectionHandler;
	}

	public DataSectionHandler getDataSectionHandler() {
		return dataSectionHandler;
	}

}
