package ch.eth.jcd.badgers.vfs.core;

public class DataBlock {

	/**
	 * Size of one DataBlock when serialized to disk
	 */
	public static final int DATA_BLOCK_SIZE = 512;

	/**
	 * Position (offset in bytes) in our file where this datablock is located
	 */
	private long location;

	/**
	 * Yes, this is a linked list
	 */
	private DataBlock nextBlock = null;

	private DataHeaderBlock header = null;

	public DataBlock(DataBlock predecessor) {
		predecessor.nextBlock = this;
	}

	public DataBlock(String pathString) {
		header = new DataHeaderBlock();
	}

	public void setPath(String pathString) {
		// TODO Auto-generated method stub

	}
}
