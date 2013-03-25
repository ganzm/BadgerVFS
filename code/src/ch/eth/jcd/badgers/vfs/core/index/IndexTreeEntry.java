package ch.eth.jcd.badgers.vfs.core.index;

import ch.eth.jcd.badgers.vfs.core.data.DataBlock;

/**
 * $Id
 * 
 * That's what's inside of an IndexBlock
 * 
 * Fixed size when serialized
 * 
 * 
 */
public class IndexTreeEntry {

	public static final long INDEX_TREE_ENTRY_HASH_SIZE = 50;
	/**
	 * Size when serialized to disk in bytes
	 * 
	 * Size of our FilePath Hash we use as Key for the b-tree <br>
	 * 8 byte Reference to Datablock
	 */
	public static final long INDEX_TREE_ENTRY_TOTAL_SIZE = INDEX_TREE_ENTRY_HASH_SIZE + 8;

	/**
	 * the fixed size key which we insert into our b-Tree
	 */
	private byte[] key;

	private long firstDataBlock;

	/**
	 * Lazy loaded reference to the first DataBlock
	 * 
	 * 
	 */
	private DataBlock firstBlock;
}
