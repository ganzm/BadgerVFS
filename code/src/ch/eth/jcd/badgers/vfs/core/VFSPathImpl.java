package ch.eth.jcd.badgers.vfs.core;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class VFSPathImpl implements VFSPath {

	private final VFSDiskManagerImpl diskMgr;

	private final String pathString;

	protected VFSPathImpl(VFSDiskManagerImpl vfsDiskManagerImpl, String absolutePathString) {
		this.diskMgr = vfsDiskManagerImpl;
		this.pathString = validateAbsolutePath(absolutePathString);
	}

	/**
	 * <ul>
	 * <li>strip leading and trailing whitespaces</li>
	 * <li>convert stuff like /path//otherpath to /path/otherpath</li>
	 * <li>cut away stuff like /./ and /../</li>
	 * <li>check if path starts with /</li>
	 * <ul>
	 */
	public static String validateAbsolutePath(String pathString) {
		// TODO do stuff here

		return pathString;
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
		// what to do
		// create hash from path string
		// ask IndexSectionHandler whether there is an entry for this hash in its tree
		// everything ok
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public boolean exists() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public VFSEntry getVFSEntry() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public String getPathString() {
		return pathString;
	}

	@Override
	public String toString() {
		return "Path to " + pathString;
	}

}
