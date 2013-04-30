/**
 * JCD Virtual File System 
 * spring 2013
 * Group: Badgers
 */
package ch.eth.jcd.badgers.vfs.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.compression.BadgersLZ77CompressionInputStream;
import ch.eth.jcd.badgers.vfs.compression.BadgersLZ77CompressionOutputStream;
import ch.eth.jcd.badgers.vfs.compression.BadgersRLECompressionInputStream;
import ch.eth.jcd.badgers.vfs.compression.BadgersRLECompressionOutputStream;
import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
import ch.eth.jcd.badgers.vfs.core.data.DataSectionHandler;
import ch.eth.jcd.badgers.vfs.core.directory.DirectoryBlock;
import ch.eth.jcd.badgers.vfs.core.directory.DirectorySectionHandler;
import ch.eth.jcd.badgers.vfs.core.header.HeaderSectionHandler;
import ch.eth.jcd.badgers.vfs.core.interfaces.FindInFolderCallback;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.core.journaling.VFSDisabledJournaling;
import ch.eth.jcd.badgers.vfs.core.journaling.VFSJournaling;
import ch.eth.jcd.badgers.vfs.core.journaling.VFSJournalingImpl;
import ch.eth.jcd.badgers.vfs.core.journaling.items.JournalItem;
import ch.eth.jcd.badgers.vfs.core.model.Compression;
import ch.eth.jcd.badgers.vfs.core.model.DiskSpaceUsage;
import ch.eth.jcd.badgers.vfs.core.model.Encryption;
import ch.eth.jcd.badgers.vfs.encryption.CaesarInputStream;
import ch.eth.jcd.badgers.vfs.encryption.CaesarOutputStream;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.exception.VFSInvalidPathException;
import ch.eth.jcd.badgers.vfs.exception.VFSOutOfMemoryException;
import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;

/**
 * class VirtualDiskManager
 * 
 * handles a single virtual disk file
 * 
 */
public final class VFSDiskManagerImpl implements VFSDiskManager {

	private static final Logger LOGGER = Logger.getLogger(VFSDiskManagerImpl.class);

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

	private VFSJournaling journaling;

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
			LOGGER.info("Create new BadgerVFS Disk on " + config.getHostFilePath());
			LOGGER.debug("Using Config " + config.toString());
			VFSDiskManagerImpl mgr = new VFSDiskManagerImpl(config);

			File file = new File(config.getHostFilePath());
			if (file.exists()) {
				throw new VFSException("Cannot create VFSDiskManager because the file already exists " + config.getHostFilePath());
			}

			mgr.virtualDiskFile = new RandomAccessFile(file, "rw");
			mgr.headerSectionHandler = HeaderSectionHandler.createNew(mgr.virtualDiskFile, config);
			long directorySectionOffset = mgr.headerSectionHandler.getSectionSize();
			long dataSectionOffset = mgr.headerSectionHandler.getDataSectionOffset();

			mgr.directorySectionHandler = DirectorySectionHandler.createNew(mgr.virtualDiskFile, config, directorySectionOffset, dataSectionOffset);
			mgr.dataSectionHandler = DataSectionHandler.createNew(mgr.virtualDiskFile, config, dataSectionOffset);

			mgr.createRootFolder();
			mgr.initJournaling();

			return mgr;

		} catch (IOException e) {
			throw new VFSException(e);
		}
	}

	private void createRootFolder() throws IOException, VFSOutOfMemoryException {
		LOGGER.debug("Creating root folder...");

		DataBlock rootDirectoryDataBlock = dataSectionHandler.allocateNewDataBlock(true);
		DirectoryBlock rootDirectoryBlock = directorySectionHandler.allocateNewDirectoryBlock();

		prepareRootFolder(rootDirectoryDataBlock, rootDirectoryBlock);

		LOGGER.debug("Creating root folder done");
	}

	private void openRootFolder() throws VFSException {
		LOGGER.debug("Opening root folder...");

		DataBlock rootDirectoryDataBlock = dataSectionHandler.loadDataBlock(dataSectionHandler.getSectionOffset());
		DirectoryBlock rootDirectoryBlock = directorySectionHandler.loadDirectoryBlock(directorySectionHandler.getSectionOffset());

		prepareRootFolder(rootDirectoryDataBlock, rootDirectoryBlock);

		LOGGER.debug("Opening root folder done");
	}

	private void prepareRootFolder(DataBlock rootDirectoryDataBlock, DirectoryBlock rootDirectoryBlock) {
		try {
			VFSPathImpl rootPath = new VFSPathImpl(this, VFSPathImpl.FILE_SEPARATOR);
			VFSDirectoryImpl rootDirectory = new VFSDirectoryImpl(this, rootPath, rootDirectoryDataBlock, rootDirectoryBlock);

			this.root = rootDirectory;
		} catch (VFSInvalidPathException e) {
			throw new VFSRuntimeException("Internal Error while preparing Root Folder", e);
		}
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
			LOGGER.info("Open BadgerVFS Disk on " + config.getHostFilePath());
			LOGGER.debug("Using Config " + config.toString());
			VFSDiskManagerImpl mgr = new VFSDiskManagerImpl(config);

			File file = new File(config.getHostFilePath());
			if (!file.exists()) {
				throw new VFSException("Cannot open VFSDiskManager because the file does not exist " + config.getHostFilePath());
			}

			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

			mgr.headerSectionHandler = HeaderSectionHandler.createExisting(randomAccessFile, config);
			long directorySectionOffset = mgr.headerSectionHandler.getSectionSize();
			long dataSectionOffset = mgr.headerSectionHandler.getDataSectionOffset();

			mgr.directorySectionHandler = DirectorySectionHandler.createExisting(randomAccessFile, config, directorySectionOffset, dataSectionOffset);
			mgr.dataSectionHandler = DataSectionHandler.createExisting(randomAccessFile, config, dataSectionOffset);

			mgr.virtualDiskFile = randomAccessFile;

			mgr.openRootFolder();
			mgr.initJournaling();

			return mgr;

		} catch (IOException e) {
			throw new VFSException(e);
		}
	}

	private void initJournaling() {
		if (config.isHostNameLinked()) {
			this.journaling = new VFSJournalingImpl(this);

		} else {
			this.journaling = new VFSDisabledJournaling();
		}
	}

	@Override
	public void close() throws VFSException {

		try {
			journaling.closeJournal();

			headerSectionHandler.close();
			directorySectionHandler.close();
			dataSectionHandler.close();

			virtualDiskFile.close();
		} catch (IOException ex) {
			throw new VFSException(ex);
		}
	}

	@Override
	public void dispose() throws VFSException {
		LOGGER.info("Getting rid of " + config.getHostFilePath());
		close();

		File file = new File(config.getHostFilePath());
		if (file.exists() && !file.delete()) {
			throw new VFSException("Could not delete File " + config.getHostFilePath());
		}
	}

	@Override
	public long getFreeSpace() throws VFSException {
		LOGGER.info("Query free space");
		try {
			long max = dataSectionHandler.getMaximumPossibleDataBlocks();
			long occupied = dataSectionHandler.getNumberOfOccupiedBlocks();

			return (max - occupied) * DataBlock.BLOCK_SIZE;
		} catch (IOException ex) {
			throw new VFSException("", ex);
		}
	}

	@Override
	public long getMaxSpace() throws VFSException {
		LOGGER.info("Query max space");
		try {
			return dataSectionHandler.getMaximumPossibleDataBlocks() * DataBlock.BLOCK_SIZE;
		} catch (IOException ex) {
			throw new VFSException("", ex);
		}
	}

	@Override
	public DiskSpaceUsage getDiskSpaceUsage() throws VFSException {
		try {
			DiskSpaceUsage du = new DiskSpaceUsage();

			long maxData = dataSectionHandler.getMaximumPossibleDataBlocks();
			du.setMaxDataBlocks(maxData);
			du.setFreeDataBlocks(maxData - dataSectionHandler.getNumberOfOccupiedBlocks());

			du.setMaxData(maxData * DataBlock.BLOCK_SIZE);
			du.setFreeData((maxData - dataSectionHandler.getNumberOfOccupiedBlocks()) * DataBlock.BLOCK_SIZE);

			du.setMaxDirectoryBlocks(directorySectionHandler.getMaxNumDirectoryBlocks());
			du.setFreeDirectoryBlocks(directorySectionHandler.getNumberOfFreeDirectoryBlocks());
			return du;
		} catch (IOException e) {
			throw new VFSException("", e);
		}
	}

	@Override
	public VFSEntry getRoot() {
		return root;
	}

	/**
	 * TODO promote to interface class and implement
	 * 
	 * <ul>
	 * <li>Compacts the DataSection of the disk</li>
	 * <li>shrinks the virtual disk file located on the host file system</li>
	 * </ul>
	 * 
	 * @throws VFSException
	 */
	public void compact() throws VFSException {
		throw new UnsupportedOperationException("TODO");
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

	@Override
	public DiskConfiguration getDiskConfiguration() throws VFSException {
		return config;
	}

	@Override
	public VFSPath createPath(String pathString) throws VFSException {
		VFSPathImpl path = new VFSPathImpl(this, pathString);
		return path;
	}

	/**
	 * apply compression/encryption checks the configuration an wraps the InputStream accordingly
	 * 
	 * @param inputStream
	 * @return
	 */
	public InputStream wrapInputStream(InputStream inputStream) {

		InputStream result = inputStream;
		Compression compressionAlgo = config.getCompressionAlgorithm();
		Encryption encryptionAlgo = config.getEncryptionAlgorithm();

		// Compression
		if (compressionAlgo == Compression.LZ77) {
			result = new BufferedInputStream(result);
			result = new BadgersLZ77CompressionInputStream(result);
		} else if (compressionAlgo == Compression.RLE) {
			result = new BufferedInputStream(result);
			result = new BadgersRLECompressionInputStream(result);
		}

		// Encryption
		if (encryptionAlgo == Encryption.CAESAR) {
			result = new CaesarInputStream(result, 3);
		}

		return result;
	}

	/**
	 * apply compression/encryption checks the configuration an wraps the OutputStream accordingly
	 * 
	 * @param outputStream
	 * @return
	 */
	public OutputStream wrapOutputStream(OutputStream outputStream) {
		Compression compressionAlgo = config.getCompressionAlgorithm();
		Encryption encryptionAlgo = config.getEncryptionAlgorithm();

		OutputStream result = outputStream;

		// Compression
		if (compressionAlgo == Compression.LZ77) {
			result = new BufferedOutputStream(result);
			result = new BadgersLZ77CompressionOutputStream(result);
		} else if (compressionAlgo == Compression.RLE) {
			result = new BufferedOutputStream(result);
			result = new BadgersRLECompressionOutputStream(result);
		}

		// Encryption
		if (encryptionAlgo == Encryption.CAESAR) {
			result = new CaesarOutputStream(result, 3);
		}

		return result;
	}

	@Override
	public void find(String fileName, FindInFolderCallback observer) throws VFSException {
		getRoot().findInFolder(fileName, observer);
	}

	@Override
	public void closeCurrentJournal() throws VFSException {
		journaling.closeJournal();
	}

	public void addJournalItem(JournalItem journalEntry) {
		journaling.addJournalItem(journalEntry);
	}

	@Override
	public List<Journal> getPendingJournals() throws VFSException {
		return journaling.getPendingJournals();
	}

	/**
	 * This method is called on an offline disk to attach and upload it to a synchronization server
	 * 
	 * @return current changes made to this disk are stored in the journal and returned
	 */
	@Override
	public Journal linkDisk(String hostName) throws VFSException {
		headerSectionHandler.setLinkedHostName(virtualDiskFile, hostName);
		config.setLinkedHostName(hostName);
		return journaling.createJournal(getRoot());
	}

	/**
	 * This method is called on the synchronization server whenever a client publishes his disk
	 * 
	 * @param journal
	 * @throws VFSException
	 */
	public void replayInitialJournal(Journal journal) throws VFSException {
		for (JournalItem item : journal.getJournalEntries()) {
			item.replay(this);
		}
	}

	@Override
	public void pauseJournaling(boolean pause) {
		journaling.pauseJournaling(pause);
	}

	@Override
	public UUID getDiskId() {
		return headerSectionHandler.getUuid();
	}
}
