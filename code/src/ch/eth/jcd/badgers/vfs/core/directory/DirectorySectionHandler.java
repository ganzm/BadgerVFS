package ch.eth.jcd.badgers.vfs.core.directory;

import java.io.IOException;
import java.io.RandomAccessFile;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;

/**
 * $Id$
 * 
 * TODO describe DirectorySectionHandler
 * 
 */
public class DirectorySectionHandler {

	/**
	 * Default size of the directory section of our file system
	 */
	public static final long DEFAULT_DIRECTORYSECTION_SIZE = DirectoryBlock.BLOCK_SIZE * 20;

	/**
	 * Size of this DirectorySection on the virtual disk, this value is fix once it is set
	 */
	private long sectionSize;

	private long sectionOffset;

	private final byte[] directoryBlockBuffer = new byte[DirectoryBlock.BLOCK_SIZE];

	private final RandomAccessFile virtualDiskFile;

	private DirectorySectionHandler(RandomAccessFile virtualDiskFile) {
		this.virtualDiskFile = virtualDiskFile;
	}

	public static DirectorySectionHandler createNew(RandomAccessFile randomAccessFile, DiskConfiguration config, long directorySectionOffset,
			long dataSectionOffset) throws IOException {
		DirectorySectionHandler directorySection = new DirectorySectionHandler(randomAccessFile);

		// set fixed size of the section
		directorySection.sectionSize = dataSectionOffset - directorySectionOffset;

		// set offset
		directorySection.sectionOffset = directorySectionOffset;

		// jump to end of file (this is currently the start of the DirectorySection)
		randomAccessFile.seek(directorySectionOffset);

		randomAccessFile.setLength(directorySectionOffset + directorySection.sectionSize);

		return directorySection;
	}

	public static DirectorySectionHandler createExisting(RandomAccessFile randomAccessFile, DiskConfiguration config, long directorySectionOffset,
			long dataSectionOffset) {
		DirectorySectionHandler directorySection = new DirectorySectionHandler(randomAccessFile);

		directorySection.sectionSize = dataSectionOffset - directorySectionOffset;

		// set offset
		directorySection.sectionOffset = directorySectionOffset;

		return directorySection;
	}

	public long getSectionSize() {
		return sectionSize;
	}

	public long getSectionOffset() {
		return sectionOffset;
	}

	public void close() {

	}

	public DirectoryBlock allocateNewDirectoryBlock() {
		long freePosition = getNextFreeDirectorySectionPosition();
		DirectoryBlock directoryBlock = new DirectoryBlock(freePosition);
		directoryBlock.persist(virtualDiskFile);
		return directoryBlock;
	}

	private long getNextFreeDirectorySectionPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	public DirectoryBlock loadDirectoryBlock(long location) throws IOException {
		virtualDiskFile.seek(location);
		virtualDiskFile.read(directoryBlockBuffer);

		DirectoryBlock dataBlock = DirectoryBlock.deserialize(location, directoryBlockBuffer);

		return dataBlock;
	}
}
