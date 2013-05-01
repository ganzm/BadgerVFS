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
 */
public class DataBlock {

	/**
	 * Size of one DataBlock when serialized to disk
	 */
	public static final int BLOCK_SIZE = 1024;

	/**
	 * Constrain number of DirectoryEntries pointing to the same file content to avoid overflow
	 */
	public static final short MAX_LINK_COUNT = 126;

	/**
	 * StartPosition of this DataBlock
	 * 
	 * 
	 * Position (offset in bytes) in our file where this DataBlock is located
	 */
	private final long location;

	/**
	 * Yes, this is a linked list
	 */
	private long nextDataBlock = 0;

	private Date creationDate;

	/**
	 * Number of UserData stored in this DataBlock
	 */
	private int dataLength = 0;

	/**
	 * If set to true this is the first DataBlock of a File or a Directory
	 * 
	 * If true we have DirectoryBlocks pointing on us
	 */
	private boolean isEntryHeaderBlock;

	/**
	 * counter from 0 to 127 which indicates how many DirectoryBlocks point on this specific DataBlock
	 */
	private short linkCount = 1;

	public DataBlock(long location, boolean isEntryHeaderBlock) {
		this.isEntryHeaderBlock = isEntryHeaderBlock;
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
	 * Location in our Virtual Disk File where the first user data bytes of this DataBlock is located
	 * 
	 * @return
	 */
	public long getUserDataLocation() {
		return location + getHeaderSize();
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
		ByteBuffer buf = ByteBuffer.allocate(getHeaderSize());

		// BlockHeader
		int header = 1;
		if (isEntryHeaderBlock) {
			header = header | 2;
		}
		buf.put((byte) header);

		// Next DataBlock
		buf.putLong(nextDataBlock);

		if (isEntryHeaderBlock) {

			// Creation Date
			buf.putLong(creationDate.getTime());

			// LinkCount
			buf.putShort(linkCount);
		}

		// Data Length
		buf.putInt(dataLength);

		return buf.array();
	}

	public static DataBlock deserialize(long location, byte[] dataBlockBuffer) {
		ByteBuffer buf = ByteBuffer.wrap(dataBlockBuffer);
		Date creationDate = null;
		short linkCount = -1;

		// BlockHeader
		int headerByte = buf.get();

		boolean isEntryHeaderBlock = (headerByte & 2) != 0;

		// Next DataBlock
		long nextBlockLocation = buf.getLong();

		if (isEntryHeaderBlock) {
			// Creation Date
			creationDate = new Date(buf.getLong());

			// LinkCount
			linkCount = buf.getShort();
		}

		// Data Length
		int dataLength = buf.getInt();

		DataBlock dataBlock = new DataBlock(location, isEntryHeaderBlock);
		dataBlock.creationDate = creationDate;
		dataBlock.linkCount = linkCount;
		dataBlock.nextDataBlock = nextBlockLocation;
		dataBlock.dataLength = dataLength;

		return dataBlock;
	}

	public void persist(RandomAccessFile virtualDiskFile) throws IOException {
		virtualDiskFile.seek(location);
		virtualDiskFile.write(serializeHeader());
	}

	private int getHeaderSize() {
		if (isEntryHeaderBlock) {
			return 23;
		} else {
			return 13;
		}
	}

	/**
	 * number of DirectoryEntries pointing to this DataBlock
	 * 
	 * @return
	 */
	public short getLinkCount() {
		return linkCount;
	}

	public void incLinkCount() {
		this.linkCount++;
	}

	public void decLinkCount() {
		this.linkCount--;
	}
}
