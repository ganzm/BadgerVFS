package ch.eth.jcd.badgers.vfs.core;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.exception.VFSInvalidPathException;

public class VFSPathImpl implements VFSPath {

	private final VFSDiskManagerImpl diskMgr;

	private final String pathString;
	private final String[] pathParts;

	protected VFSPathImpl(VFSDiskManagerImpl vfsDiskManagerImpl, String absolutePathString) {
		this.diskMgr = vfsDiskManagerImpl;
		this.pathString = absolutePathString;
		this.pathParts = validateAndSplitAbsolutePath(absolutePathString);
	}

	/**
	 * <ul>
	 * <li>strip leading and trailing whitespaces</li>
	 * <li>convert stuff like /path//otherpath to /path/otherpath</li>
	 * <li>cut away stuff like /./ and /../</li>
	 * <li>check if path starts with /</li>
	 * <ul>
	 */
	public static String[] validateAndSplitAbsolutePath(String pathString) {
		// TODO do stuff here

		pathString = pathString.trim();

		if (pathString.startsWith(VFSPath.FILE_SEPARATOR) == false) {
			throw new VFSInvalidPathException("Path should start with / but it does not" + pathString);
		}

		// TODO this wont work properly for every case
		pathString = pathString.substring(1);

		return pathString.split(VFSPath.FILE_SEPARATOR);
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

		VFSEntry current = diskMgr.getRoot();

		for (String pathItem : pathParts) {
			//
			// current.getChildren()
			// "1".toString();

		}

		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public VFSEntry getVFSEntry() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public String getAbsolutePath() {
		return pathString;
	}

	@Override
	public String toString() {
		return "Path to " + pathString;
	}

	@Override
	public String getName() throws VFSException {
		return pathString.substring(pathString.lastIndexOf(VFSPath.FILE_SEPARATOR) + 1);
	}
}
