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

	/** the file we write our data to */
	private RandomAccessFile virtualDiskFile;

	private HeaderSectionHandler header;

	private IndexSectionHandler index;

	private DataSectionHandler data;

	/**
	 * Private constructor
	 */
	private VFSDiskManagerImpl() {

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
			VFSDiskManagerImpl mgr = new VFSDiskManagerImpl();

			File file = new File(config.getHostFilePath());
			if (file.exists()) {
				throw new VFSException("Cannot create VFSDiskManager because the file already exists " + config.getHostFilePath());
			}

			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

			long headerSectionOffset = 0;
			mgr.header = HeaderSectionHandler.createNew(randomAccessFile, config, headerSectionOffset);
			long indexSectionOffset = mgr.header.getSectionSize();
			mgr.index = IndexSectionHandler.createNew(randomAccessFile, config, indexSectionOffset);
			long dataSectionOffset = indexSectionOffset + mgr.index.getSectionSize();
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

		// TODO implement me
		throw new UnsupportedOperationException();
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
	public void dispose() {
		// TODO Auto-generated method stub

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
