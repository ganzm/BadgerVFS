package ch.eth.jcd.badgers.vfs.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;

public class VFSEntryImpl implements VFSEntry {

	private final VFSPath path;
	private final VFSDiskManagerImpl diskManager;

	private DataBlock firstDataBlock;

	/**
	 * creates a new
	 * 
	 * @param path
	 */
	protected VFSEntryImpl(VFSDiskManagerImpl diskManager, VFSPath path) {
		this.diskManager = diskManager;
		this.path = path;
	}

	/**
	 * TODO move to me a factory if you want to
	 * 
	 * @param vfsPathImpl
	 * @return
	 */
	protected static VFSEntryImpl createNewDirectory(VFSDiskManagerImpl diskManager, VFSPathImpl vfsPathImpl) {
		throw new UnsupportedOperationException("TODO");
		// VFSEntryImpl entry = new VFSEntryImpl(diskManager, vfsPathImpl);
		//
		// DataSectionHandler dataSectionHandler = diskManager.getDataSectionHandler();
		//
		// DataBlock dataBlock = dataSectionHandler.allocateNewDataBlock()
		// dataBlock.setPath(vfsPathImpl.getPathString());
		//
		// return entry;
	}

	public void setDataBlock(DataBlock dataBlock) {
		if (firstDataBlock != null) {
			// this should not happen
			throw new VFSRuntimeException("Internal error - Overriding DataBlock of " + this);
		}

		this.firstDataBlock = dataBlock;
	}

	@Override
	public void copyTo(VFSPath newLocation) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public List<VFSEntry> getChildren() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public InputStream getInputStream() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public OutputStream getOutputStream(int writeMode) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public VFSPath getPath() {
		return path;
	}

	@Override
	public void moveTo(VFSPath path) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void renameTo(String newName) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void delete() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public boolean isDirectory() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public VFSPath getNewChildPath(String childName) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public VFSEntry getParent() {
		throw new UnsupportedOperationException("TODO");
	}
}
