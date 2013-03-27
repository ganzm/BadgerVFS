package ch.eth.jcd.badgers.vfs.core.directory;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSInvalidPathException;

/**
 * $Id
 * 
 * That's what's inside of a DirectoryBlock
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
	 * Points to a DataBlock in our file
	 */
	private long dataBlockLocation;

	/**
	 * 
	 * Is zero if this Directory does not specify a Folder
	 * 
	 * Otherwise it points to a DirectoryBlock which is the root of a B-Tree which contains the files located in our folder
	 * 
	 * 
	 */
	private long directoryEntryNodeLocation;

	// /**
	// * DataBlock which is linked to this DirectoryEntry
	// */
	// private DataBlock dataBlock;
	//
	// /**
	// * If this DirectoryEntryBlock is a Folder then this link points to the root of a B-Tree which lists the contained Entries of this Folder
	// */
	// private DirectoryBlock directoryEntryTreeNode;

	public DirectoryEntryBlock(String fileName) {
		this.fileName = fileName;
		checkFileNameConstraints(fileName);
	}

	private void checkFileNameConstraints(String fileName) {
		if (fileName.contains(VFSPath.FILE_SEPARATOR)) {
			throw new VFSInvalidPathException(fileName + " is an invalid FileName");
		}
	}

	public boolean isFolderEntryBlock() {
		return directoryEntryNodeLocation != 0;
	}

	public String getFileName() {
		return fileName;
	}

	public long getDataBlockLocation() {
		return dataBlockLocation;
	}

	public long getDirectoryEntryNodeLocation() {
		return directoryEntryNodeLocation;
	}
}
