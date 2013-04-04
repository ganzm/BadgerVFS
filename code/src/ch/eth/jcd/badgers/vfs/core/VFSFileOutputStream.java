package ch.eth.jcd.badgers.vfs.core;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
import ch.eth.jcd.badgers.vfs.core.data.DataSectionHandler;

public class VFSFileOutputStream extends OutputStream {

	private static final Logger LOGGER = Logger.getLogger(VFSFileOutputStream.class);

	private final DataSectionHandler dataSectionHandler;
	private final DataBlock firstDataBlock;
	private DataBlock currentDataBlock;

	private long currentPosition;

	public VFSFileOutputStream(DataSectionHandler dataSectionHandler, DataBlock firstDataBlock) {
		this.dataSectionHandler = dataSectionHandler;
		this.firstDataBlock = firstDataBlock;
		this.currentDataBlock = firstDataBlock;
		this.currentPosition = firstDataBlock.getUserDataLocation();

		LOGGER.info("OutputStream[" + firstDataBlock.getLocation() + "] - Open");
	}

	@Override
	public void write(int b) throws IOException {
		long spaceLeftOnThisBlock = currentDataBlock.getLocation() + DataBlock.BLOCK_SIZE - currentPosition;

		if (spaceLeftOnThisBlock <= 0) {
			DataBlock newBlock = dataSectionHandler.allocateNewDataBlock(false);
			currentDataBlock.setNextDataBlock(newBlock.getLocation());
			dataSectionHandler.persistDataBlock(currentDataBlock);

			currentDataBlock = newBlock;
			currentPosition = currentDataBlock.getUserDataLocation();

			LOGGER.info("OutputStream[" + firstDataBlock.getLocation() + "] - allocated new Block at " + currentDataBlock.getLocation());
		}

		dataSectionHandler.writeByte(currentPosition++, b);
		currentDataBlock.addDataLength(1);

		// updated header information (file size)
		// this is quite inefficient but works
		dataSectionHandler.persistDataBlock(currentDataBlock);
	}

	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		int endIndex = off + len;
		int i = off;
		while (i < endIndex) {

			long spaceLeftOnThisBlock = currentDataBlock.getLocation() + DataBlock.BLOCK_SIZE - currentPosition;
			// remaining data to write on this call
			int remainingData = endIndex - i;

			int toWriteOnThisBlock = Math.min(remainingData, (int) spaceLeftOnThisBlock);

			dataSectionHandler.write(currentPosition, b, i, toWriteOnThisBlock);
			currentPosition += toWriteOnThisBlock;

			currentDataBlock.addDataLength(toWriteOnThisBlock);

			if (spaceLeftOnThisBlock < remainingData) {
				DataBlock newBlock = dataSectionHandler.allocateNewDataBlock(false);
				currentDataBlock.setNextDataBlock(newBlock.getLocation());
				dataSectionHandler.persistDataBlock(currentDataBlock);

				currentDataBlock = newBlock;
				currentPosition = currentDataBlock.getUserDataLocation();

				LOGGER.info("OutputStream[" + firstDataBlock.getLocation() + "] - allocated new Block at " + currentDataBlock.getLocation());
			}

			i += toWriteOnThisBlock;
		}

		// updated header information (file size)
		// this is quite inefficient but works
		dataSectionHandler.persistDataBlock(currentDataBlock);
	}
}
