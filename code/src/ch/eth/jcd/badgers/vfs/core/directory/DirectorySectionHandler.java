package ch.eth.jcd.badgers.vfs.core.directory;

import java.io.IOException;
import java.io.RandomAccessFile;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.util.HashUtil;

public class DirectorySectionHandler {

	/**
	 * Default size of the index section of our file system
	 */
	public static final long DEFAULT_INDEXSECTION_SIZE = DirectoryBlock.INDEX_BLOCK_SIZE * 20;

	/**
	 * Size of the Index Section on the virtual disk, this value is fix once it is set
	 */
	private long indexSectionSize;

	private DirectorySectionHandler() {

	}

	public static DirectorySectionHandler createNew(RandomAccessFile randomAccessFile, DiskConfiguration config, long indexSectionOffset, long dataSectionOffset)
			throws IOException {
		DirectorySectionHandler index = new DirectorySectionHandler();

		index.indexSectionSize = dataSectionOffset - indexSectionOffset;
		randomAccessFile.seek(indexSectionOffset);
		randomAccessFile.setLength(indexSectionOffset + index.indexSectionSize);

		return index;
	}

	public static DirectorySectionHandler createExisting(RandomAccessFile randomAccessFile, DiskConfiguration config, long indexSectionOffset,
			long dataSectionOffset) {
		DirectorySectionHandler index = new DirectorySectionHandler();

		index.indexSectionSize = dataSectionOffset - indexSectionOffset;

		return index;
	}

	public long getSectionSize() {
		return indexSectionSize;
	}

	public void close() {

	}

	public DirectoryEntryBlock getIndexTreeEntryByPathString(String pathString) {

		byte[] key = HashUtil.hashUtf8String(pathString);
		throw new UnsupportedOperationException("TODO");
	}
}
