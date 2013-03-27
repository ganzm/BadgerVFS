package ch.eth.jcd.badgers.vfs.core.directory;

import java.io.RandomAccessFile;

/**
 * 
 * $Id$
 * 
 * DirectoryBlocks are located in the DirectorySection of the file system.
 * 
 * All DirectoryBlocks have constant size {@link DirectoryBlock#BLOCK_SIZE}
 * 
 */
public class DirectoryBlock {

	/**
	 * Size of one IndexBlock when serialized to disk
	 * 
	 * An Index Block consists of 2 IndexTreeEntries and 3 pointers to other IndexBlocks
	 */
	public static final int BLOCK_SIZE = (2 * DirectoryEntryBlock.BLOCK_SIZE) + (3 * 8);

	/**
	 * Position (offset in bytes) in our file where this IndexBlock is located
	 */
	private final long location;

	private long linkLeft;
	private long linkMiddle;
	private long linkRight;

	private DirectoryEntryBlock nodeLeft;
	private DirectoryEntryBlock nodeRight;

	public DirectoryBlock(long location) {
		this.location = location;
	}

	public void persist(RandomAccessFile virtualDiskFile) {
		// TODO

	}

	public static DirectoryBlock deserialize(long location, byte[] directoryBlockBuffer) {

		DirectoryBlock block = new DirectoryBlock(location);
		// TODO
		return block;
	}

	public long getLocation() {
		return location;
	}

	public long getLinkLeft() {
		return linkLeft;
	}

	public void setLinkLeft(long linkLeft) {
		this.linkLeft = linkLeft;
	}

	public long getLinkMiddle() {
		return linkMiddle;
	}

	public void setLinkMiddle(long linkMiddle) {
		this.linkMiddle = linkMiddle;
	}

	public long getLinkRight() {
		return linkRight;
	}

	public void setLinkRight(long linkRight) {
		this.linkRight = linkRight;
	}

	public DirectoryEntryBlock getNodeLeft() {
		return nodeLeft;
	}

	public void setNodeLeft(DirectoryEntryBlock nodeLeft) {
		this.nodeLeft = nodeLeft;
	}

	public DirectoryEntryBlock getNodeRight() {
		return nodeRight;
	}

	public void setNodeRight(DirectoryEntryBlock nodeRight) {
		this.nodeRight = nodeRight;
	}

}
