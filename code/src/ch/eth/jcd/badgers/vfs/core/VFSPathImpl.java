package ch.eth.jcd.badgers.vfs.core;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class VFSPathImpl implements VFSPath {

	private final VFSDiskManagerImpl diskMgr;

	private final String pathString;

	protected VFSPathImpl(VFSDiskManagerImpl vfsDiskManagerImpl, String pathString) {
		this.diskMgr = vfsDiskManagerImpl;
		this.pathString = pathString;
	}

	@Override
	public VFSEntry createDirectory() throws VFSException {

		if (exists()) {
			throw new VFSException("Can't create Directory from " + pathString + " already exists");
		}

		VFSEntryImpl entry = VFSEntryImpl.createNewDirectory(diskMgr, this);

		return entry;
	}

	@Override
	public VFSEntry createFile() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public boolean exists() {

		// what to do
		// create hash from path string
		// ask IndexSectionHandler whether there is an entry for this hash in its tree
		// everything ok

		throw new UnsupportedOperationException("TODO");

	}

	@Override
	public VFSEntry getVFSEntry() {
		throw new UnsupportedOperationException("TODO");
	}

	public String getPathString() {
		return pathString;
	}

	@Override
	public String toString() {
		return "Path to " + pathString;
	}
}
