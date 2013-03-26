package ch.eth.jcd.badgers.vfs.core.directory;

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
public class DirectoryEntryBlock {

	/**
	 * Hash Size in Bytes
	 * 
	 * Sha512 creates 512bit hashes
	 */
	public static final long INDEX_TREE_ENTRY_HASH_SIZE = 64;
	/**
	 * Size when serialized to disk in bytes
	 * 
	 * Size of our FilePath Hash we use as Key for the b-tree <br>
	 * 8 byte Reference to DataBlock
	 */
	public static final long INDEX_TREE_ENTRY_TOTAL_SIZE = INDEX_TREE_ENTRY_HASH_SIZE + 8;

	/**
	 * the fixed size key which we insert into our b-Tree
	 */
	private byte[] key;

	private long firstDataBlock;

	/**
	 * Lazy loaded reference to the first DataBlock
	 */
	private DataBlock firstBlock;

	public DirectoryEntryBlock() {

	}
}
