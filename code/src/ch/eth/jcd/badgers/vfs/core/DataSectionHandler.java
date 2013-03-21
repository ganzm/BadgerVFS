package ch.eth.jcd.badgers.vfs.core;

import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;

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

	private DataSectionHandler() {

	}

	public static DataSectionHandler createNew(RandomAccessFile randomAccessFile, DiskConfiguration config, long dataSectionOffset) {
		DataSectionHandler data = new DataSectionHandler();

		data.dataSectionOffset = dataSectionOffset;

		// init size to 0 and don't allocate any space
		data.dataSectionSize = 0;

		return data;
	}

	public long getSectionOffset() {
		return dataSectionOffset;
	}

	public long getSectionSize() {
		return dataSectionSize;
	}

	public void close() {
		// TODO Auto-generated method stub

	}

}
