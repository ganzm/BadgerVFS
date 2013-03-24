package ch.eth.jcd.badgers.vfs.core;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;

/**
 * $Id$
 * 
 * TODO describe DataSectionHandler
 * 
 */
public class DataSectionHandler {
	private static Logger logger = Logger.getLogger(DataSectionHandler.class);

	/**
	 * offset in bytes where the first byte of the data section is located on the virtual disk
	 */
	private long dataSectionOffset;

	/**
	 * Size in bytes of the data section, may grow or shrink over time
	 */
	private long dataSectionSize;

	/**
	 * Constructor
	 */
	private DataSectionHandler() {

	}

	public static DataSectionHandler createExisting(RandomAccessFile randomAccessFile, DiskConfiguration config, long dataSectionOffset) throws IOException {
		logger.debug("read Data Section...");
		DataSectionHandler data = new DataSectionHandler();

		data.dataSectionOffset = dataSectionOffset;

		// init size to 0 and don't allocate any space
		data.dataSectionSize = randomAccessFile.length() - dataSectionOffset;

		logger.debug("read Data Section DONE");
		return data;
	}

	public static DataSectionHandler createNew(RandomAccessFile randomAccessFile, DiskConfiguration config, long dataSectionOffset) {
		logger.debug("create Data Section...");
		DataSectionHandler data = new DataSectionHandler();

		data.dataSectionOffset = dataSectionOffset;

		// init size to 0 and don't allocate any space
		data.dataSectionSize = 0;

		logger.debug("create Data Section DONE");
		return data;
	}

	/**
	 * Memory Allocation on the DataSection
	 * 
	 * @return
	 */
	public DataBlock allocateNewDataBlock() {
		throw new UnsupportedOperationException("TODO");
	}

	public void close() {
	}

	public long getSectionOffset() {
		return dataSectionOffset;
	}

	public long getSectionSize() {
		return dataSectionSize;
	}

}
