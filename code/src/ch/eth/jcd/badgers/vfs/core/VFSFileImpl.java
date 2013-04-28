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
import ch.eth.jcd.badgers.vfs.core.journaling.items.ModifyFileItem;
import ch.eth.jcd.badgers.vfs.core.model.SearchParameter;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

/**
 * $Id$
 * 
 * This class represents a single file located on the virtual file system
 * 
 */
public class VFSFileImpl extends VFSEntryImpl {

	private static final Logger LOGGER = Logger.getLogger(VFSFileImpl.class);

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

		// if (writeMode == WRITE_MODE_OVERRIDE) {
		try {
			truncateDataBlocks();
		} catch (IOException e) {
			throw new VFSException("Error while truncating file", e);
		}
		// }

		VFSFileOutputStream outputStream = new VFSFileOutputStream(diskManager.getDataSectionHandler(), firstDataBlock);
		diskManager.addJournalItem(new ModifyFileItem(this));
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

			VFSFileImpl newFile = (VFSFileImpl) newLocation.createFile();

			// since we do an file system internal copy we bypass the getOutputStream/getInputStream method to avoid compression/decompression
			// encryption/decryption
			// get outputstream of new file
			out = new VFSFileOutputStream(diskManager.getDataSectionHandler(), newFile.firstDataBlock);
			// get input stream of this file
			in = new VFSFileInputStream(diskManager.getDataSectionHandler(), firstDataBlock);

			byte[] buffer = new byte[DataBlock.USERDATA_SIZE];

			int numBytes;
			while ((numBytes = in.read(buffer)) >= 0) {
				out.write(buffer, 0, numBytes);
			}

			diskManager.addJournalItem(new ModifyFileItem(newFile));
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

	@Override
	public void findInFolder(SearchParameter searchParameter, FindInFolderCallback observer) throws VFSException {
		throw new VFSException("find operation not supported for files");
	}
}
