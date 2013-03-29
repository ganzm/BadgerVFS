package ch.eth.jcd.badgers.vfs.core.directory;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.exception.VFSOutOfMemoryException;

/**
 * $Id$
 * 
 * TODO describe DirectorySectionHandler
 * 
 */
public class DirectorySectionHandler {

	private static Logger logger = Logger.getLogger(DirectorySectionHandler.class);

	public static final long MAX_NUM_DIRECTORY_BLOCKS = 20;
	/**
	 * Default size of the directory section of our file system
	 */
	public static final long DEFAULT_DIRECTORYSECTION_SIZE = DirectoryBlock.BLOCK_SIZE * MAX_NUM_DIRECTORY_BLOCKS;

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

	public DirectoryBlock allocateNewDirectoryBlock() throws IOException {
		long freePosition = getNextFreeDirectorySectionPosition();
		DirectoryBlock directoryBlock = new DirectoryBlock(freePosition);
		directoryBlock.persist(virtualDiskFile);
		return directoryBlock;
	}

	private long getNextFreeDirectorySectionPosition() throws IOException {

		// go to start of DataSection
		virtualDiskFile.seek(sectionOffset);

		long currentLocation = sectionOffset;
		long maxNumDirectoryBlocks = sectionSize / DirectoryBlock.BLOCK_SIZE;

		for (int i = 0; i < maxNumDirectoryBlocks; i++) {
			int byteAsInt = virtualDiskFile.read();
			if ((byteAsInt & 1) != 0) {
				// block already occupied
			} else {
				// block free
				logger.debug("Found free DirectoryBlock at " + currentLocation + " Block Nr " + ((currentLocation - sectionOffset) / DirectoryBlock.BLOCK_SIZE));
				return currentLocation;
			}

			int skipedBytes = virtualDiskFile.skipBytes(DirectoryBlock.BLOCK_SIZE - 1);
			currentLocation = virtualDiskFile.getFilePointer();

			if (skipedBytes != DirectoryBlock.BLOCK_SIZE - 1) {
				throw new VFSOutOfMemoryException("There is no more space left on the DirectorySection");
			}

			byteAsInt = virtualDiskFile.read();
		}

		throw new VFSOutOfMemoryException("There is no more space left on the DirectorySection");
	}

	public DirectoryBlock loadDirectoryBlock(long location) throws IOException {
		virtualDiskFile.seek(location);
		virtualDiskFile.read(directoryBlockBuffer);

		DirectoryBlock dataBlock = DirectoryBlock.deserialize(location, directoryBlockBuffer);

		return dataBlock;
	}
}
