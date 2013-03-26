package ch.eth.jcd.badgers.vfs.core.directory;

import ch.eth.jcd.badgers.vfs.core.data.DataBlock;

/**
 * 
 * $Id
 * 
 * IndexBlocks are located in the IndexSection of the file system. All IndexBlocks have constant size {@link DataBlock#DATA_BLOCK_SIZE}
 * 
 */
public class DirectoryBlock {

	/**
	 * Size of one IndexBlock when serialized to disk
	 * 
	 * An Index Block consists of 2 IndexTreeEntries and 3 pointers to other IndexBlocks
	 */
	public static final long INDEX_BLOCK_SIZE = (2 * DirectoryEntryBlock.INDEX_TREE_ENTRY_TOTAL_SIZE) + (3 * 8);

	/**
	 * Position (offset in bytes) in our file where this IndexBlock is located
	 */
	private long location;

	private DirectoryBlock link1;
	private DirectoryEntryBlock node2;
	private DirectoryBlock link3;
	private DirectoryEntryBlock node4;
	private DirectoryBlock link5;

	public DirectoryBlock() {

	}
}
