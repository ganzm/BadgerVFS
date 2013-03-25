package ch.eth.jcd.badgers.vfs.core.index;

import ch.eth.jcd.badgers.vfs.core.data.DataBlock;

/**
 * 
 * $Id
 * 
 * IndexBlocks are located in the IndexSection of the file system. All IndexBlocks have constant size {@link DataBlock#DATA_BLOCK_SIZE}
 * 
 */
public class IndexBlock {

	/**
	 * Size of one IndexBlock when serialized to disk
	 * 
	 * An Index Block consists of 2 IndexTreeEntries and 3 pointers to other IndexBlocks
	 */
	public static final long INDEX_BLOCK_SIZE = (2 * IndexTreeEntry.INDEX_TREE_ENTRY_TOTAL_SIZE) + (3 * 8);

	/**
	 * Position (offset in bytes) in our file where this IndexBlock is located
	 */
	private long location;

	private IndexBlock link1;
	private IndexTreeEntry node2;
	private IndexBlock link3;
	private IndexTreeEntry node4;
	private IndexBlock link5;

	public IndexBlock() {

	}
}
