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

	private DirectoryBlock link1;
	private DirectoryEntryBlock node2;
	private DirectoryBlock link3;
	private DirectoryEntryBlock node4;
	private DirectoryBlock link5;

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

	public DirectoryBlock getLink1() {
		return link1;
	}

	public void setLink1(DirectoryBlock link1) {
		this.link1 = link1;
	}

	public DirectoryEntryBlock getNode2() {
		return node2;
	}

	public void setNode2(DirectoryEntryBlock node2) {
		this.node2 = node2;
	}

	public DirectoryBlock getLink3() {
		return link3;
	}

	public void setLink3(DirectoryBlock link3) {
		this.link3 = link3;
	}

	public DirectoryEntryBlock getNode4() {
		return node4;
	}

	public void setNode4(DirectoryEntryBlock node4) {
		this.node4 = node4;
	}

	public DirectoryBlock getLink5() {
		return link5;
	}

	public void setLink5(DirectoryBlock link5) {
		this.link5 = link5;
	}

	public long getLocation() {
		return location;
	}

}
