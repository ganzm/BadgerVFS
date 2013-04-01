package ch.eth.jcd.badgers.vfs.core;

import java.io.IOException;

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

		if (VFSPath.FILE_SEPARATOR.equals(pathString)) {
			// apply exception for root path
			return new String[0];
		}

		if (pathString.startsWith(VFSPath.FILE_SEPARATOR) == false) {
			throw new VFSInvalidPathException("Path should start with / but it does not Path: " + pathString);
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

		try {
			VFSEntryImpl entry = VFSEntryImpl.createNewDirectory(diskMgr, this);
			return entry;
		} catch (IOException e) {
			throw new VFSException(e);
		}

	}

	@Override
	public VFSEntry createFile() throws VFSException {
		if (exists()) {
			throw new VFSException("Can't create File from " + pathString + " already exists");
		}

		try {
			VFSFileImpl entry = VFSEntryImpl.createNewFile(diskMgr, this);
			return entry;
		} catch (IOException e) {
			throw new VFSException(e);
		}
	}

	public String getParentPath() throws VFSException {
		String parentPath = pathString.substring(0, pathString.lastIndexOf(VFSPath.FILE_SEPARATOR));
		if ("".equals(parentPath)) {
			// found the root path
			return VFSPath.FILE_SEPARATOR;
		}
		return parentPath;
	}

	@Override
	public boolean exists() throws VFSException {
		// we need to loop through the while directory tree to determine whether a path exists or not
		VFSEntryImpl current = (VFSEntryImpl) diskMgr.getRoot();
		for (String pathItem : pathParts) {
			VFSEntryImpl child = current.getChildByName(pathItem);
			if (child == null) {
				return false;
			}

			current = child;
		}
		return true;

	}

	@Override
	public VFSEntry getVFSEntry() throws VFSException {
		VFSEntryImpl rootEntry = (VFSEntryImpl) diskMgr.getRoot();
		if (rootEntry.getPath().equals(this)) {
			return rootEntry;
		}

		VFSEntryImpl current = rootEntry;
		for (String pathPart : pathParts) {
			current = current.getChildByName(pathPart);
		}

		return current;
	}

	/**
	 * Eclipse generated code
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pathString == null) ? 0 : pathString.hashCode());
		return result;
	}

	/**
	 * Eclipse generated code
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VFSPathImpl other = (VFSPathImpl) obj;
		if (pathString == null) {
			if (other.pathString != null)
				return false;
		} else if (!pathString.equals(other.pathString))
			return false;
		return true;
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
