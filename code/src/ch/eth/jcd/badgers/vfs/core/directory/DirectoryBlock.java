package ch.eth.jcd.badgers.vfs.core.directory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

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
	 * An Index Block consists of 1 Header 2 IndexTreeEntries and 3 pointers to other IndexBlocks
	 */
	public static final int BLOCK_SIZE = 1 + (2 * DirectoryEntryBlock.BLOCK_SIZE) + (3 * 8);

	/**
	 * Position (offset in bytes) in our file where this IndexBlock is located
	 */
	private final long location;

	/**
	 * TODO specification missing
	 */
	private final int header = 255;

	private long linkLeft;
	private long linkMiddle;
	private long linkRight;

	private DirectoryEntryBlock nodeLeft;
	private DirectoryEntryBlock nodeRight;

	public DirectoryBlock(long location) {
		this.location = location;
	}

	public void persist(RandomAccessFile virtualDiskFile) throws IOException {
		virtualDiskFile.seek(location);
		virtualDiskFile.write(serialize());
	}

	private byte[] serialize() {
		ByteBuffer buf = ByteBuffer.allocate(BLOCK_SIZE);

		// write 1 header byte
		buf.put((byte) header);

		// write left link
		buf.putLong(linkLeft);

		// write left node
		if (nodeLeft != null) {
			buf.put(nodeLeft.serialize());
		} else {
			buf.position(buf.position() + DirectoryEntryBlock.BLOCK_SIZE);
		}

		// write middle node
		buf.putLong(linkMiddle);

		// write right node
		if (nodeRight != null) {
			buf.put(nodeRight.serialize());
		} else {
			buf.position(buf.position() + DirectoryEntryBlock.BLOCK_SIZE);
		}

		// write right link
		buf.putLong(linkRight);

		return buf.array();
	}

	public static DirectoryBlock deserialize(long location, byte[] directoryBlockBuffer) {
		ByteBuffer buf = ByteBuffer.wrap(directoryBlockBuffer);
		int headerByte = buf.get();

		if ((headerByte & 1) == 0) {
			// no header found - there is no DirectoryBlock
			return null;
		}

		DirectoryBlock block = new DirectoryBlock(location);

		block.linkLeft = buf.getLong();
		block.nodeLeft = DirectoryEntryBlock.deserialize(buf);
		block.linkMiddle = buf.getLong();
		block.nodeRight = DirectoryEntryBlock.deserialize(buf);
		block.linkRight = buf.getLong();

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

	public void dumpShort(DirectorySectionHandler directorySectionHandler, StringBuffer buf, int depth) throws IOException {
		for (int i = 0; i < depth; i++) {
			buf.append("\t");
		}

		buf.append("Block[");
		buf.append(location);
		buf.append("] LinkL[");

		buf.append(linkLeft);

		buf.append("] LinkM[");
		buf.append(linkMiddle);
		buf.append("] LinkR[");
		buf.append(linkRight);
		buf.append("] NodeL[");

		if (nodeLeft != null) {
			buf.append(nodeLeft.getFileName());

		}
		buf.append("] NodeR[");
		if (nodeRight != null) {
			buf.append(nodeRight.getFileName());

		}
		buf.append("]\n");

		if (linkLeft != 0) {
			DirectoryBlock block = directorySectionHandler.loadDirectoryBlock(linkLeft);
			block.dumpShort(directorySectionHandler, buf, depth + 1);
		}

		if (linkMiddle != 0) {
			DirectoryBlock block = directorySectionHandler.loadDirectoryBlock(linkMiddle);
			block.dumpShort(directorySectionHandler, buf, depth + 1);
		}

		if (linkRight != 0) {
			DirectoryBlock block = directorySectionHandler.loadDirectoryBlock(linkRight);
			block.dumpShort(directorySectionHandler, buf, depth + 1);
		}
	}

	@Override
	public String toString() {
		return "DirBlock[" + location + "] LinkLeft[" + linkLeft + "] Node[" + nodeLeft + "] LinkMiddle[" + linkMiddle + "] Node [" + nodeRight
				+ "] LinkRight[" + linkRight + "]";
	}
}
