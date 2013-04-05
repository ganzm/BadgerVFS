package ch.eth.jcd.badgers.vfs.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
import ch.eth.jcd.badgers.vfs.core.interfaces.FindInFolderCallback;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

/**
 * $Id$
 * 
 * 
 * TODO describe VFSFileImpl
 * 
 */
public class VFSFileImpl extends VFSEntryImpl {

	private static final Logger LOGGER = Logger.getLogger(VFSDiskManagerImpl.class);

	protected VFSFileImpl(VFSDiskManagerImpl diskManager, VFSPathImpl path, DataBlock firstDataBlock) {
		super(diskManager, path, firstDataBlock);
	}

	@Override
	public List<VFSEntry> getChildren() {
		LOGGER.debug("Tried to call getChildren on File " + getPath());
		return null;
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public VFSEntryImpl getChildByName(String fileName) {
		LOGGER.debug("Tried to call getChildByName on File " + getPath());
		return null;
	}

	@Override
	public String toString() {
		return "File " + path;
	}

	@Override
	public InputStream getInputStream() throws VFSException {
		VFSFileInputStream inputStream = new VFSFileInputStream(diskManager.getDataSectionHandler(), firstDataBlock);
		return diskManager.wrapInputStream(inputStream);
	}

	@Override
	public OutputStream getOutputStream(int writeMode) throws VFSException {
		VFSFileOutputStream outputStream = new VFSFileOutputStream(diskManager.getDataSectionHandler(), firstDataBlock);
		return diskManager.wrapOutputStream(outputStream);

	}

	@Override
	public void copyTo(VFSPath newLocation) throws VFSException {
		if (newLocation.exists()) {
			throw new VFSException("Copy failed - file already exist " + newLocation.getAbsolutePath());
		}

		LOGGER.info("Copy file " + path.getAbsolutePath() + " to " + newLocation.getAbsolutePath());
		OutputStream out = null;
		InputStream in = null;

		try {

			VFSEntry newFile = newLocation.createFile();

			out = newFile.getOutputStream(WRITE_MODE_OVERRIDE);
			in = getInputStream();

			byte[] buffer = new byte[DataBlock.USERDATA_SIZE];

			int numBytes;
			while ((numBytes = in.read(buffer)) >= 0) {
				out.write(buffer, 0, numBytes);
			}

		} catch (IOException ex) {
			throw new VFSException("Error while copying data from " + path.getAbsolutePath() + " to " + newLocation.getAbsolutePath(), ex);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					LOGGER.error("", e);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOGGER.error("", e);
				}
			}
		}
	}

	@Override
	public void findInFolder(String fileName, FindInFolderCallback observer) throws VFSException {
		throw new VFSException("find operation not supported for files");
	}

}
