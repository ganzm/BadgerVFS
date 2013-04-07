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
import ch.eth.jcd.badgers.vfs.encryption.CaesarInputStream;
import ch.eth.jcd.badgers.vfs.encryption.CaesarOutputStream;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

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

			return mgr;

		} catch (IOException e) {
			throw new VFSException(e);
		}
	}

	private void createRootFolder() throws IOException {
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
		VFSPathImpl rootPath = new VFSPathImpl(this, VFSPathImpl.FILE_SEPARATOR);
		VFSDirectoryImpl rootDirectory = new VFSDirectoryImpl(this, rootPath, rootDirectoryDataBlock, rootDirectoryBlock);

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

			return mgr;

		} catch (IOException e) {
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
		String compressionAlgoName = config.getCompressionAlgorithm();
		String encryptionAlgoName = config.getEncryptionAlgorithm();

		// Compression
		if (DiskConfiguration.COMPRESSION_LZ77.equals(compressionAlgoName)) {
			result = new BufferedInputStream(result);
			result = new BadgersLZ77CompressionInputStream(result);
		} else if (DiskConfiguration.COMPRESSION_RLE.equals(compressionAlgoName)) {
			result = new BufferedInputStream(result);
			result = new BadgersRLECompressionInputStream(result);
		}

		// Encryption
		if (DiskConfiguration.ENCRYPTION_CAESAR.equals(encryptionAlgoName)) {
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
		String compressionAlgoName = config.getCompressionAlgorithm();
		String encryptionAlgoName = config.getEncryptionAlgorithm();

		OutputStream result = outputStream;

		// Compression
		if (DiskConfiguration.COMPRESSION_LZ77.equals(compressionAlgoName)) {
			result = new BufferedOutputStream(result);
			result = new BadgersLZ77CompressionOutputStream(result);
		} else if (DiskConfiguration.COMPRESSION_RLE.equals(compressionAlgoName)) {
			result = new BufferedOutputStream(result);
			result = new BadgersRLECompressionOutputStream(result);
		}

		// Encryption
		if (DiskConfiguration.ENCRYPTION_CAESAR.equals(encryptionAlgoName)) {
			result = new CaesarOutputStream(result, 3);
		}

		return result;
	}

	@Override
	public void find(String fileName, FindInFolderCallback observer) throws VFSException {
		getRoot().findInFolder(fileName, observer);
	}

}
