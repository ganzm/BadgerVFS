package ch.eth.jcd.badgers.vfs.core.data;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.exception.VFSInvalidLocationExceptionException;
import ch.eth.jcd.badgers.vfs.exception.VFSOutOfMemoryException;
import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;

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
	private final long dataSectionOffset;

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

	private static final long BLOCK_INCREMENT = 100;

	/**
	 * Constructor
	 * 
	 * @throws IOException
	 */
	private DataSectionHandler(RandomAccessFile virtualDiskFile, long maximumFileSize, long dataSectionOffset, long dataSectionSize) {
		this.virtualDiskFile = virtualDiskFile;
		this.maximumFileSize = maximumFileSize;
		this.dataSectionOffset = dataSectionOffset;
		this.dataSectionSize = dataSectionSize;

		if (dataSectionSize == 0) {
			this.cache = new DataBlockCache();
		} else {
			this.cache = new DataBlockCache(dataSectionOffset, dataSectionOffset + dataSectionSize - DataBlock.BLOCK_SIZE);

		}
	}

	public static DataSectionHandler createExisting(RandomAccessFile randomAccessFile, DiskConfiguration config, long dataSectionOffset) throws IOException {
		LOGGER.debug("read Data Section...");

		// init size to 0 and don't allocate any space
		long dataSectionSize = randomAccessFile.length() - dataSectionOffset;

		DataSectionHandler data = new DataSectionHandler(randomAccessFile, config.getMaximumSize(), dataSectionOffset, dataSectionSize);

		if (data.dataSectionSize % DataBlock.BLOCK_SIZE != 0) {
			throw new VFSRuntimeException("Expected DataSection Size to be dividible by BlockSize " + DataBlock.BLOCK_SIZE + " DataSectionSize: "
					+ data.dataSectionSize);
		}

		LOGGER.debug("read Data Section DONE");
		return data;
	}

	public static DataSectionHandler createNew(RandomAccessFile randomAccessFile, DiskConfiguration config, long dataSectionOffset) {
		LOGGER.debug("create Data Section...");
		// init size to 0 and don't allocate any space
		long dataSectionSize = 0;

		DataSectionHandler data = new DataSectionHandler(randomAccessFile, config.getMaximumSize(), dataSectionOffset, dataSectionSize);

		LOGGER.debug("create Data Section DONE");
		return data;
	}

	/**
	 * Memory Allocation on the DataSection
	 * 
	 * @param isEntryHeaderBlock
	 *            If set to true this is the first DataBlock of a File or a Directory
	 * 
	 * @return
	 * @throws IOException
	 */
	public DataBlock allocateNewDataBlock(boolean isEntryHeaderBlock) throws IOException, VFSOutOfMemoryException {
		long freePosition = getNextFreeDataBlockPosition();
		DataBlock dataBlock = new DataBlock(freePosition, isEntryHeaderBlock);
		dataBlock.persist(virtualDiskFile);
		// update cache
		cache.markOccupied(freePosition);

		return dataBlock;
	}

	public void freeDataBlock(DataBlock dataBlock) throws IOException {
		virtualDiskFile.seek(dataBlock.getLocation());
		// just clear header byte
		virtualDiskFile.write(0);
		// update cache
		cache.markFree(dataBlock.getLocation());
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

	private DataBlockCache cache;

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private long getNextFreeDataBlockPosition() throws IOException, VFSOutOfMemoryException {
		DataBlockCacheEntry entry;

		entry = cache.getNextFreeOrUnkownDataBlocks();
		while (entry != null) {
			// check cache
			if (entry.getState() == DataBlockCacheEntryState.FREE) {
				// lucky me - block is free
				return entry.getFirstBlockLocation();
			} else if (entry.getState() == DataBlockCacheEntryState.UNKNOWN) {
				// cache does not know about the state of the block
				// so go and have a look
				for (long location = entry.getFirstBlockLocation(); location <= entry.getLastBlockLocation(); location += DataBlock.BLOCK_SIZE) {
					virtualDiskFile.seek(location);
					int byteAsInt = virtualDiskFile.read();

					if (byteAsInt >= 0) {
						if ((byteAsInt & 1) == 0) {
							// block free
							LOGGER.debug("Found free DataBlock at " + location + " Block Nr " + (location - dataSectionOffset) / DataBlock.BLOCK_SIZE);
							return location;

						} else {
							// found DataBlock Header Byte
							// update cache so we don't have to check this block next time
							cache.markOccupied(location);
						}
					}
				}
			}
			// else if (entry.getState() == DataBlockCacheEntryState.OCCUPIED) {
			//
			// }
			entry = cache.getNextFreeOrUnkownDataBlocks();
		}

		// all the available DataBlocks are occupied
		// increase Disk

		long endOfFilePos = virtualDiskFile.length();

		long tmpBlockIncrement = BLOCK_INCREMENT;
		if (maximumFileSize > 0) {
			// there is a restriction about file size
			long maxAllowedNewBlocks = (maximumFileSize - endOfFilePos) / DataBlock.BLOCK_SIZE;
			tmpBlockIncrement = Math.min(maxAllowedNewBlocks, tmpBlockIncrement);
		}

		if (tmpBlockIncrement <= 0) {
			throw new VFSOutOfMemoryException("Reached maximum file size of " + maximumFileSize + " bytes");
		}

		long newLength = endOfFilePos + tmpBlockIncrement * DataBlock.BLOCK_SIZE;
		virtualDiskFile.setLength(newLength);

		cache.addFreeBlocks(endOfFilePos, endOfFilePos + DataBlock.BLOCK_SIZE * (tmpBlockIncrement - 1));

		dataSectionSize = newLength - dataSectionOffset;
		LOGGER.info("Expanded VirtualDiskFile by " + tmpBlockIncrement + " DataBlocks to " + virtualDiskFile.length());

		LOGGER.debug("Found free DataBlock at " + endOfFilePos + " Block Nr " + (endOfFilePos - dataSectionOffset) / DataBlock.BLOCK_SIZE);
		return endOfFilePos;
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

	public int read(long position, byte[] b, int off, int len) throws IOException {
		virtualDiskFile.seek(position);
		return virtualDiskFile.read(b, off, len);
	}

	public void writeByte(long position, int b) throws IOException {
		virtualDiskFile.seek(position);
		virtualDiskFile.write(b);
	}

	public void write(long position, byte[] b, int off, int len) throws IOException {
		virtualDiskFile.seek(position);
		virtualDiskFile.write(b, off, len);
	}

	public void persistDataBlock(DataBlock dataBlock) throws IOException {
		dataBlock.persist(virtualDiskFile);
	}

	/**
	 * Return the number of DataBlocks this virtual disk can hold at max
	 * 
	 * @return
	 * @throws IOException
	 */
	public long getMaximumPossibleDataBlocks() throws IOException {
		long canGrow;
		if (maximumFileSize > 0) {
			canGrow = maximumFileSize - virtualDiskFile.length();
		} else {
			canGrow = virtualDiskFile.length();
		}
		return (dataSectionSize + canGrow) / DataBlock.BLOCK_SIZE;
	}

	public long getNumberOfOccupiedBlocks() throws IOException {

		// go to start of DataSection
		virtualDiskFile.seek(dataSectionOffset);

		long occupiedBlockCount = 0;

		int byteAsInt = virtualDiskFile.read();
		while (byteAsInt >= 0) {
			if ((byteAsInt & 1) != 0) {

				occupiedBlockCount++;
			}

			int skipedBytes = virtualDiskFile.skipBytes(DataBlock.BLOCK_SIZE - 1);
			byteAsInt = virtualDiskFile.read();

			if (skipedBytes < 0) {
				break;
				// end of file
			}
		}

		return occupiedBlockCount;
	}
}
