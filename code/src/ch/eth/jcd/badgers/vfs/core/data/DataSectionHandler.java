package ch.eth.jcd.badgers.vfs.core.data;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.exception.VFSInvalidLocationExceptionException;
import ch.eth.jcd.badgers.vfs.exception.VFSOutOfMemoryException;

/**
 * $Id$
 * 
 * TODO describe DataSectionHandler
 * 
 */
public final class DataSectionHandler {
	private static final Logger LOGGER = Logger.getLogger(DataSectionHandler.class);

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
	 * Maximum Disk Space we are allowed to use
	 * 
	 * is <= 0 if there are no restrictions
	 * 
	 * @see DiskConfiguration#setMaximumSize(long)
	 */
	private final long maximumFileSize;

	private static final long BLOCK_INCREMENT = 2;

	/**
	 * Constructor
	 */
	private DataSectionHandler(RandomAccessFile virtualDiskFile, long maximumFileSize) {
		this.virtualDiskFile = virtualDiskFile;
		this.maximumFileSize = maximumFileSize;
	}

	public static DataSectionHandler createExisting(RandomAccessFile randomAccessFile, DiskConfiguration config, long dataSectionOffset) throws IOException {
		LOGGER.debug("read Data Section...");
		DataSectionHandler data = new DataSectionHandler(randomAccessFile, config.getMaximumSize());

		data.dataSectionOffset = dataSectionOffset;

		// init size to 0 and don't allocate any space
		data.dataSectionSize = randomAccessFile.length() - dataSectionOffset;

		LOGGER.debug("read Data Section DONE");
		return data;
	}

	public static DataSectionHandler createNew(RandomAccessFile randomAccessFile, DiskConfiguration config, long dataSectionOffset) {
		LOGGER.debug("create Data Section...");
		DataSectionHandler data = new DataSectionHandler(randomAccessFile, config.getMaximumSize());

		data.dataSectionOffset = dataSectionOffset;

		// init size to 0 and don't allocate any space
		data.dataSectionSize = 0;

		LOGGER.debug("create Data Section DONE");
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

	public void freeDataBlock(DataBlock dataBlock) throws IOException {
		virtualDiskFile.seek(dataBlock.getLocation());
		// just clear header byte
		virtualDiskFile.write(0);
	}

	public DataBlock loadDataBlock(long location) throws VFSException {
		if (location == 0 || location < dataSectionOffset || location > (dataSectionOffset + dataSectionSize)) {
			throw new VFSInvalidLocationExceptionException("Tried to load DataBlock from Location " + location + " Valid Range is [" + dataSectionOffset + ", "
					+ dataSectionOffset + dataSectionSize);
		}

		try {
			virtualDiskFile.seek(location);
			virtualDiskFile.read(dataBlockBuffer);

			DataBlock dataBlock = DataBlock.deserialize(location, dataBlockBuffer);

			return dataBlock;
		} catch (IOException e) {
			throw new VFSException(e);
		}
	}

	/**
	 * TODO implement caching here
	 * 
	 * @return
	 * @throws IOException
	 */
	private long getNextFreeDataBlockPosition() throws IOException {

		// go to start of DataSection
		virtualDiskFile.seek(dataSectionOffset);

		long currentLocation = dataSectionOffset;

		int byteAsInt = virtualDiskFile.read();

		while (byteAsInt >= 0) {
			if ((byteAsInt & 1) == 0) {
				// block free
				LOGGER.debug("Found free DataBlock at " + currentLocation + " Block Nr " + (currentLocation - dataSectionOffset) / DataBlock.BLOCK_SIZE);
				return currentLocation;
			} // else: block already occupied

			int skipedBytes = virtualDiskFile.skipBytes(DataBlock.BLOCK_SIZE - 1);
			currentLocation = virtualDiskFile.getFilePointer();

			if (skipedBytes != DataBlock.BLOCK_SIZE - 1) {
				throw new VFSOutOfMemoryException("There is no more space left on the DataSection");
			}

			byteAsInt = virtualDiskFile.read();
		}

		// end of file and still no free DataBlock found
		long currentFilePosition = virtualDiskFile.getFilePointer();

		long tmpBlockIncrement = BLOCK_INCREMENT;
		if (maximumFileSize > 0) {
			long maxAllowedNewBlocks = (maximumFileSize - currentFilePosition) / DataBlock.BLOCK_SIZE;
			tmpBlockIncrement = Math.min(maxAllowedNewBlocks, tmpBlockIncrement);
		}

		long newLength = currentFilePosition + tmpBlockIncrement * DataBlock.BLOCK_SIZE;
		virtualDiskFile.setLength(newLength);
		dataSectionSize = newLength - dataSectionOffset;
		LOGGER.info("Expanded VirtualDiskFile by " + tmpBlockIncrement + " DataBlocks to " + virtualDiskFile.length());

		LOGGER.debug("Found free DataBlock at " + currentLocation + " Block Nr " + (currentLocation - dataSectionOffset) / DataBlock.BLOCK_SIZE);
		return currentFilePosition;

	}

	public void close() {
	}

	public long getSectionOffset() {
		return dataSectionOffset;
	}

	public long getSectionSize() {
		return dataSectionSize;
	}

	public int readByte(long position) throws IOException {
		virtualDiskFile.seek(position);
		return virtualDiskFile.read();
	}

	public void writeByte(long position, int b) throws IOException {
		virtualDiskFile.seek(position);
		virtualDiskFile.write(b);
	}

	public void persistDataBlock(DataBlock dataBlock) throws IOException {
		dataBlock.persist(virtualDiskFile);
	}
}
