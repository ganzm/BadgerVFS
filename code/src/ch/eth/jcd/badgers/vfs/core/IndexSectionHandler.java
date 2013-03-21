package ch.eth.jcd.badgers.vfs.core;

import java.io.IOException;
import java.io.RandomAccessFile;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;

public class IndexSectionHandler {

	/**
	 * Default size of the index section of our file system
	 */
	public static final long DEFAULT_INDEXSECTION_SIZE = IndexNode.SIZE_IN_BYTES * 20;

	/**
	 * Size of the Index Section on the virtual disk, this value is fix once it is set
	 */
	private long indexSectionSize;

	private IndexSectionHandler() {

	}

	public static IndexSectionHandler createNew(RandomAccessFile randomAccessFile, DiskConfiguration config, long indexSectionOffset, long dataSectionOffset)
			throws IOException {
		IndexSectionHandler index = new IndexSectionHandler();

		index.indexSectionSize = dataSectionOffset - indexSectionOffset;
		randomAccessFile.seek(indexSectionOffset);
		randomAccessFile.setLength(indexSectionOffset + index.indexSectionSize);

		return index;
	}

	public static IndexSectionHandler createExisting(RandomAccessFile randomAccessFile, DiskConfiguration config, long indexSectionOffset,
			long dataSectionOffset) {
		IndexSectionHandler index = new IndexSectionHandler();

		index.indexSectionSize = dataSectionOffset - indexSectionOffset;

		return index;
	}

	public long getSectionSize() {
		return indexSectionSize;
	}

	public void close() {
		// TODO Auto-generated method stub

	}

}
