package ch.eth.jcd.badgers.vfs.core.data;

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

	private final byte[] dataBlockBuffer = new byte[DataBlock.BLOCK_SIZE];

	/**
	 * offset in bytes where the first byte of the data section is located on the virtual disk
	 */
	private long dataSectionOffset;

	/**
	 * Size in bytes of the data section, may grow or shrink over time
	 */
	private long dataSectionSize;

	private final RandomAccessFile virtualDiskFile;

	/**
	 * Constructor
	 */
	private DataSectionHandler(RandomAccessFile virtualDiskFile) {
		this.virtualDiskFile = virtualDiskFile;
	}

	public static DataSectionHandler createExisting(RandomAccessFile randomAccessFile, DiskConfiguration config, long dataSectionOffset) throws IOException {
		logger.debug("read Data Section...");
		DataSectionHandler data = new DataSectionHandler(randomAccessFile);

		data.dataSectionOffset = dataSectionOffset;

		// init size to 0 and don't allocate any space
		data.dataSectionSize = randomAccessFile.length() - dataSectionOffset;

		logger.debug("read Data Section DONE");
		return data;
	}

	public static DataSectionHandler createNew(RandomAccessFile randomAccessFile, DiskConfiguration config, long dataSectionOffset) {
		logger.debug("create Data Section...");
		DataSectionHandler data = new DataSectionHandler(randomAccessFile);

		data.dataSectionOffset = dataSectionOffset;

		// init size to 0 and don't allocate any space
		data.dataSectionSize = 0;

		logger.debug("create Data Section DONE");
		return data;
	}

	/**
	 * Memory Allocation on the DataSection
	 * 
	 * @param isDirectory
	 * 
	 * @return
	 * @throws IOException
	 */
	public DataBlock allocateNewDataBlock(boolean isDirectory) throws IOException {
		long freePosition = getNextFreeDataBlockPosition();
		DataBlock dataBlock = new DataBlock(freePosition, isDirectory);
		dataBlock.persist(virtualDiskFile);
		return dataBlock;
	}

	public DataBlock loadDataBlock(long location) throws IOException {

		virtualDiskFile.seek(location);
		virtualDiskFile.read(dataBlockBuffer);

		DataBlock dataBlock = DataBlock.deserialize(location, dataBlockBuffer);

		return dataBlock;
	}

	private long getNextFreeDataBlockPosition() {
		// TODO Auto-generated method stub
		return 0;
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
