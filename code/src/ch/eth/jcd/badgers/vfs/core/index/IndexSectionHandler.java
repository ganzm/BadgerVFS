package ch.eth.jcd.badgers.vfs.core.index;

import java.io.IOException;
import java.io.RandomAccessFile;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.util.HashUtil;

public class IndexSectionHandler {

	/**
	 * Default size of the index section of our file system
	 */
	public static final long DEFAULT_INDEXSECTION_SIZE = IndexBlock.INDEX_BLOCK_SIZE * 20;

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

	}

	public IndexTreeEntry getIndexTreeEntryByPathString(String pathString) {

		byte[] key = HashUtil.hashUtf8String(pathString);
		throw new UnsupportedOperationException("TODO");
	}

}
