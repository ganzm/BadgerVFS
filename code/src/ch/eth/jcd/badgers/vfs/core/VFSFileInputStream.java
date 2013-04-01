package ch.eth.jcd.badgers.vfs.core;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
import ch.eth.jcd.badgers.vfs.core.data.DataSectionHandler;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class VFSFileInputStream extends InputStream {
	private static Logger LOGGER = Logger.getLogger(VFSFileInputStream.class);

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
			long bytesLeftOnThisBlock = currentPosition - currentDataBlock.getUserDataLocation();

			while (bytesLeftOnThisBlock <= 0) {
				long nextBlockLocation = currentDataBlock.getNextDataBlock();

				if (nextBlockLocation != 0) {

					// skip to next Block
					currentDataBlock = dataSectionHandler.loadDataBlock(nextBlockLocation);
					LOGGER.info("InputStream[" + firstDataBlock.getLocation() + "] - Skip to next DataBlock " + currentDataBlock.getLocation());

					currentPosition = currentDataBlock.getUserDataLocation();

					bytesLeftOnThisBlock = currentPosition - currentDataBlock.getUserDataLocation();
				} else {
					// no more DataBlocks

					// EOF
					return -1;
				}

			}

			return dataSectionHandler.readByte(currentPosition++);
		} catch (VFSException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		// TODO implement to speed things up
		return super.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {

		// TODO implement to speed things up
		return super.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		// TODO Auto-generated method stub
		return super.skip(n);
	}

}
