package ch.eth.jcd.badgers.vfs.core.directory;

import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSInvalidPathException;

/**
 * $Id
 * 
 * That's what's inside of an IndexBlock
 * 
 * Fixed size when serialized
 * 
 */
public class DirectoryEntryBlock {

	/**
	 * Maximum size of a file name in bytes
	 */
	public static final int MAX_FILENAME_SIZE = 112;

	/**
	 * Size of one IndexBlock when serialized to disk
	 * 
	 * An Index Block consists of 2 IndexTreeEntries and 3 pointers to other IndexBlocks
	 */
	public static final int BLOCK_SIZE = MAX_FILENAME_SIZE + (2 * 8);

	private final String fileName;

	/**
	 * DataBlock which is linked to this DirectoryEntry
	 */
	private DataBlock dataBlock;

	/**
	 * If this DirectoryEntryBlock is a Folder then this link points to the root of a B-Tree which lists the contained Entries of this Folder
	 */
	private DirectoryBlock directoryEntryTreeNode;

	public DirectoryEntryBlock(String fileName) {
		this.fileName = fileName;
		checkFileNameConstraints(fileName);
	}

	private void checkFileNameConstraints(String fileName) {
		if (fileName.contains(VFSPath.FILE_SEPARATOR)) {
			throw new VFSInvalidPathException(fileName + " is an invalid FileName");
		}
	}

	public void setDataBlock(DataBlock dataBlock) {
		this.dataBlock = dataBlock;
	}
}
