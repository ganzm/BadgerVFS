package ch.eth.jcd.badgers.vfs.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

/**
 * $Id$
 * 
 * 
 * TODO describe VFSFileImpl
 * 
 */
public class VFSFileImpl extends VFSEntryImpl {

	private static Logger LOGGER = Logger.getLogger(VFSDiskManagerImpl.class);

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
}
