package ch.eth.jcd.badgers.vfs.core.data;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 * 
 * $Id
 * 
 * DataBlocks are located in the DataSection of the file system. All DataBlocks have constant size {@link DataBlock#BLOCK_SIZE}
 * 
 */
public class DataBlock {

	/**
	 * Size of one DataBlock when serialized to disk
	 */
	public static final int BLOCK_SIZE = 512;

	/**
	 * Position (offset in bytes) in our file where this datablock is located
	 */
	private final long location;

	private final boolean isDirectory;

	/**
	 * Yes, this is a linked list
	 */
	private long nextDataBlock = 0;

	private Date creationDate;

	/**
	 * TODO
	 */
	private int dataLength = 0;

	public DataBlock(long location, boolean isDirectory) {
		this.isDirectory = isDirectory;
		this.location = location;
		this.creationDate = new Date();
	}

	public long getLocation() {
		return location;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	private byte[] serializeHeader() {
		ByteBuffer buf = ByteBuffer.allocate(21);

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
		buf.putInt(0);

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
