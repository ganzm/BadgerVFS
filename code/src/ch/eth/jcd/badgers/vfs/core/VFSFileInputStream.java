package ch.eth.jcd.badgers.vfs.core;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
import ch.eth.jcd.badgers.vfs.core.data.DataSectionHandler;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class VFSFileInputStream extends InputStream {
	private static final Logger LOGGER = Logger.getLogger(VFSFileInputStream.class);

	private final DataSectionHandler dataSectionHandler;
	private final DataBlock firstDataBlock;
	private DataBlock currentDataBlock;

	private long currentPosition;

	public VFSFileInputStream(DataSectionHandler dataSectionHandler, DataBlock firstDataBlock) {
		this.dataSectionHandler = dataSectionHandler;
		this.firstDataBlock = firstDataBlock;
		this.currentDataBlock = firstDataBlock;
		this.currentPosition = firstDataBlock.getUserDataLocation();

		LOGGER.info("InputStream[" + firstDataBlock.getLocation() + "] - Open");
	}

	@Override
	public int read() throws IOException {
		try {
			long bytesLeftOnThisBlock = currentDataBlock.getDataLenght() + currentDataBlock.getUserDataLocation() - currentPosition;

			while (bytesLeftOnThisBlock <= 0) {
				long nextBlockLocation = currentDataBlock.getNextDataBlock();

				if (nextBlockLocation == 0) {
					// no more DataBlocks
					// EOF
					return -1;
				} else {
					bytesLeftOnThisBlock = skipBlock(nextBlockLocation);
				}

			}

			return dataSectionHandler.readByte(currentPosition++);
		} catch (VFSException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		try {
			long bytesLeftOnThisBlock = currentDataBlock.getDataLenght() + currentDataBlock.getUserDataLocation() - currentPosition;

			while (bytesLeftOnThisBlock <= 0) {
				long nextBlockLocation = currentDataBlock.getNextDataBlock();

				if (nextBlockLocation == 0) {
					// no more DataBlocks
					// EOF
					return -1;
				} else {
					bytesLeftOnThisBlock = skipBlock(nextBlockLocation);
				}

			}

			int numBytesToRead = Math.min(len, (int) bytesLeftOnThisBlock);
			int numBytes = dataSectionHandler.read(currentPosition, b, off, numBytesToRead);

			currentPosition += numBytes;

			return numBytes;
		} catch (VFSException ex) {
			throw new IOException(ex);
		}
	}

	private long skipBlock(long nextBlockLocation) throws VFSException {
		long bytesLeftOnThisBlock;
		// skip to next Block
		currentDataBlock = dataSectionHandler.loadDataBlock(nextBlockLocation);
		LOGGER.trace("InputStream[" + firstDataBlock.getLocation() + "] - Jump to next DataBlock " + currentDataBlock.getLocation());

		currentPosition = currentDataBlock.getUserDataLocation();

		bytesLeftOnThisBlock = currentDataBlock.getDataLenght() + currentDataBlock.getUserDataLocation() - currentPosition;
		return bytesLeftOnThisBlock;
	}
}
