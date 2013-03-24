package ch.eth.jcd.badgers.vfs.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;

public class VFSEntryImpl implements VFSEntry {

	private final VFSPath path;
	private final VFSDiskManagerImpl diskManager;

	/**
	 * creates a new
	 * 
	 * @param path
	 */
	private VFSEntryImpl(VFSDiskManagerImpl diskManager, VFSPath path) {
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
		VFSEntryImpl entry = new VFSEntryImpl(diskManager, vfsPathImpl);

		DataSectionHandler dataSectionHandler = diskManager.getDataSectionHandler();

		DataBlock dataBlock = dataSectionHandler.allocateNewDataBlock();
		dataBlock.setPath(vfsPathImpl.getPathString());

		return entry;
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
		throw new UnsupportedOperationException("TODO");
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
}
