package ch.eth.jcd.badgers.vfs.core.directory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSInvalidPathException;

/**
 * $Id$
 * 
 * That's what's inside of a DirectoryBlock
 * 
 * Fixed size when serialized
 * 
 */
public class DirectoryEntryBlock implements Comparable<DirectoryEntryBlock> {

	private static final Charset CS = Charset.forName("UTF8");
	/**
	 * Maximum size of a file name in bytes
	 */
	public static final int MAX_FILENAME_SIZE = 112;

	/**
	 * Size of one IndexBlock when serialized to disk
	 * 
	 * An Index Block consists of 2 IndexTreeEntries and 3 pointers to other IndexBlocks
	 */
	public static final int BLOCK_SIZE = MAX_FILENAME_SIZE + 2 * 8;

	private String fileName;

	/**
	 * Points to a DataBlock in our file
	 */
	private long dataBlockLocation;

	/**
	 * Is zero if this Directory does not specify a Folder
	 * 
	 * Otherwise it points to a DirectoryBlock which is the root of a B-Tree which contains the files located in our folder
	 * 
	 */
	private long directoryEntryNodeLocation;

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

	public void setFileName(String fileName) {
		this.fileName = fileName;

	}

	public long getDataBlockLocation() {
		return dataBlockLocation;
	}

	public long getDirectoryEntryNodeLocation() {
		return directoryEntryNodeLocation;
	}

	public void assignDataBlock(DataBlock dataBlock) {
		this.dataBlockLocation = dataBlock.getLocation();
	}

	public void assignDirectoryBlock(DirectoryBlock directoryBlock) {
		this.directoryEntryNodeLocation = directoryBlock.getLocation();
	}

	@Override
	public int compareTo(DirectoryEntryBlock o) {
		return this.fileName.compareTo(o.fileName);
	}

	public byte[] serialize() {
		ByteBuffer buf = ByteBuffer.allocate(BLOCK_SIZE);

		buf.putLong(dataBlockLocation);
		buf.putLong(directoryEntryNodeLocation);
		buf.put(fileName.getBytes(CS));

		return buf.array();
	}

	public static DirectoryEntryBlock deserialize(ByteBuffer buf) {
		long newDataBlockLocation = buf.getLong();
		if (newDataBlockLocation == 0) {
			return null;
		}

		long newDirectoryEntryNodeLocation = buf.getLong();

		byte[] fileNameBuffer = new byte[MAX_FILENAME_SIZE];
		buf.get(fileNameBuffer);
		String newFileName = new String(fileNameBuffer, CS).trim();

		DirectoryEntryBlock newBlock = new DirectoryEntryBlock(newFileName);
		newBlock.dataBlockLocation = newDataBlockLocation;
		newBlock.directoryEntryNodeLocation = newDirectoryEntryNodeLocation;

		return newBlock;
	}

	public void dump(StringBuffer buf, int depth) {
		for (int i = 0; i < depth; i++) {
			buf.append('\t');
		}

		buf.append("Data[");
		buf.append(dataBlockLocation);
		buf.append("] DirRootBlock[");
		buf.append(directoryEntryNodeLocation);
		buf.append("] ");
		buf.append(fileName);
		buf.append('\n');
	}

	public static void dumpEmpty(StringBuffer buf, int depth) {
		for (int i = 0; i < depth; i++) {
			buf.append('\t');
		}

		buf.append("NO DATA\n");
	}

	@Override
	public String toString() {
		boolean isZero = directoryEntryNodeLocation != 0;
		return "DirectoryEntry Data[" + dataBlockLocation + "] IsDir[" + isZero + "] Name[" + fileName + ']';
	}

}
