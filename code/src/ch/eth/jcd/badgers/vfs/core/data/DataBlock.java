package ch.eth.jcd.badgers.vfs.core.data;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 * 
 * $Id$
 * 
 * DataBlocks are located in the DataSection of the file system. All DataBlocks have constant size {@link DataBlock#BLOCK_SIZE}
 * 
 */
public class DataBlock {

	/**
	 * Size of one DataBlock when serialized to disk
	 */
	public static final int BLOCK_SIZE = 1024;

	public static final int HEADER_SIZE = 21;

	public static final int USERDATA_SIZE = BLOCK_SIZE - HEADER_SIZE;

	/**
	 * Position (offset in bytes) in our file where this DataBlock is located
	 */
	private final long location;

	private final boolean isDirectory;

	/**
	 * Yes, this is a linked list
	 */
	private long nextDataBlock = 0;

	private Date creationDate;

	/**
	 */
	private int dataLength = 0;

	public DataBlock(long location, boolean isDirectory) {
		this.isDirectory = isDirectory;
		this.location = location;
		this.creationDate = new Date();
	}

	/**
	 * Location in our Virtual Disk File where this DataBlock is located
	 * 
	 * @return
	 */
	public long getLocation() {
		return location;
	}

	/**
	 * Location in our Virtual Disk File where the first user data bytes fo this DataBlock is located
	 * 
	 * @return
	 */
	public long getUserDataLocation() {
		return location + HEADER_SIZE;
	}

	/**
	 * Number of user data stored on this block
	 * 
	 * maximum BLOCK_SIZE - HEADER_SIZE
	 * 
	 * @return
	 */
	public int getDataLenght() {
		return dataLength;
	}

	public void addDataLength(int increment) {
		dataLength += increment;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public long getNextDataBlock() {
		return nextDataBlock;
	}

	public void setNextDataBlock(long nextDataBlock) {
		this.nextDataBlock = nextDataBlock;
	}

	private byte[] serializeHeader() {
		ByteBuffer buf = ByteBuffer.allocate(HEADER_SIZE);

		// BlockHeader
		int header = 1;
		if (isDirectory) {
			header = header | 2;
		}
		buf.put((byte) header);

		// Next DataBlock
		buf.putLong(nextDataBlock);

		// Creation Date
		buf.putLong(creationDate.getTime());

		// Data Length
		buf.putInt(dataLength);

		return buf.array();
	}

	public static DataBlock deserialize(long location, byte[] dataBlockBuffer) {
		ByteBuffer buf = ByteBuffer.wrap(dataBlockBuffer);

		// BlockHeader
		int headerByte = buf.get();

		boolean isDirectory = (headerByte & 2) != 0;

		// Next DataBlock
		long nextBlockLocation = buf.getLong();

		// Creation Date
		Date creationDate = new Date(buf.getLong());

		// Data Length
		int dataLength = buf.getInt();

		DataBlock dataBlock = new DataBlock(location, isDirectory);
		dataBlock.creationDate = creationDate;
		dataBlock.nextDataBlock = nextBlockLocation;
		dataBlock.dataLength = dataLength;

		return dataBlock;
	}

	public void persist(RandomAccessFile virtualDiskFile) throws IOException {
		virtualDiskFile.seek(location);
		virtualDiskFile.write(serializeHeader());
	}

}
