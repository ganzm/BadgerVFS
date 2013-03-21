/**
 * JCD Virtual File System 
 * spring 2013
 * Group: Badgers
 */
package ch.eth.jcd.badgers.vfs.core;

import java.io.File;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
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

	private HeaderSectionHandler header;

	private IndexSectionHandler index;

	private DataSectionHandler data;

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

			mgr.header = HeaderSectionHandler.createNew(randomAccessFile, config, headerSectionOffset);
			long indexSectionOffset = mgr.header.getSectionSize();
			long dataSectionOffset = mgr.header.getDataSectionOffset();

			mgr.index = IndexSectionHandler.createNew(randomAccessFile, config, indexSectionOffset, dataSectionOffset);
			mgr.data = DataSectionHandler.createNew(randomAccessFile, config, dataSectionOffset);

			mgr.virtualDiskFile = randomAccessFile;

			return mgr;

		} catch (Exception e) {
			throw new VFSException(e);
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
			logger.info("Open BadgerVFS Disk on " + config.getHostFilePath());
			logger.debug("Using Config " + config.toString());
			VFSDiskManagerImpl mgr = new VFSDiskManagerImpl(config);

			File file = new File(config.getHostFilePath());
			if (file.exists() == false) {
				throw new VFSException("Cannot open VFSDiskManager because the file does not exist " + config.getHostFilePath());
			}

			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

			long headerSectionOffset = 0;

			mgr.header = HeaderSectionHandler.createExisting(randomAccessFile, config, headerSectionOffset);
			long indexSectionOffset = mgr.header.getSectionSize();
			long dataSectionOffset = mgr.header.getDataSectionOffset();

			mgr.index = IndexSectionHandler.createExisting(randomAccessFile, config, indexSectionOffset, dataSectionOffset);
			mgr.data = DataSectionHandler.createExisting(randomAccessFile, config, dataSectionOffset);

			mgr.virtualDiskFile = randomAccessFile;

			return mgr;

		} catch (Exception e) {
			throw new VFSException(e);
		}
	}

	@Override
	public void close() throws VFSException {

		try {
			header.close();
			index.close();
			data.close();

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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public VFSEntry getRoot() {
		// TODO Auto-generated method stub
		return null;
	}

}
