package ch.eth.jcd.badgers.vfs.core;

import java.io.IOException;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.exception.VFSInvalidPathException;

/**
 * Represents an absolute path to a file or a directory
 * 
 * '/' acts as a directory separator char <br/>
 * The root directory's path is '/'<br/>
 * If a path points to a directory it may end with a '/' but doesn't have to<br/>
 * If a path points to a file it should not end with a '/'
 * 
 * @see VFSPath#FILE_SEPARATOR
 */
public class VFSPathImpl implements VFSPath {

	private final VFSDiskManagerImpl diskMgr;

	private final String pathString;
	private final String[] pathParts;

	protected VFSPathImpl(VFSDiskManagerImpl vfsDiskManagerImpl, String absolutePathString) throws VFSInvalidPathException {
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
	 * 
	 * @throws VFSInvalidPathException
	 */
	public static String[] validateAndSplitAbsolutePath(String pathString) throws VFSInvalidPathException {
		// TODO do stuff here

		String tmpPathString = pathString.trim();

		if (VFSPath.FILE_SEPARATOR.equals(tmpPathString)) {
			// apply exception for root path
			return new String[0];
		}

		if (!tmpPathString.startsWith(VFSPath.FILE_SEPARATOR)) {
			throw new VFSInvalidPathException("Path should start with / but it does not Path: " + tmpPathString);
		}

		// TODO this wont work properly for every case
		tmpPathString = tmpPathString.substring(1);

		return tmpPathString.split(VFSPath.FILE_SEPARATOR);
	}

	@Override
	public VFSEntry createDirectory() throws VFSException {
		if (exists()) {
			throw new VFSException("Can't create Directory from " + pathString + " - directory already exists");
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

	@Override
	public String getParentPath() throws VFSException {
		if (VFSPath.FILE_SEPARATOR.equals(pathString)) {
			// found the root path
			return VFSPath.FILE_SEPARATOR;
		}

		if (pathParts.length <= 1) {
			return VFSPath.FILE_SEPARATOR;
		}

		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < pathParts.length - 1; i++) {
			buf.append(VFSPath.FILE_SEPARATOR);
			buf.append(pathParts[i]);
		}

		return buf.toString();
	}

	@Override
	public boolean exists() throws VFSException {
		// we need to loop through the whole directory tree to determine whether a path exists or not
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
			if (current == null) {
				return null;
			}
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
	public String getName() {
		if (pathParts.length <= 0) {
			// found the root path
			return "";
		}

		return pathParts[pathParts.length - 1];
	}

	/**
	 * creates a new Path object containing the same path but a different file name
	 * 
	 * @param newFileName
	 * @return
	 * @throws VFSInvalidPathException
	 */
	public VFSPathImpl renameTo(String newFileName) throws VFSInvalidPathException {

		if (pathParts.length <= 0) {

			return new VFSPathImpl(diskMgr, VFSPath.FILE_SEPARATOR + newFileName);
		}

		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < pathParts.length - 1; i++) {
			buf.append(VFSPath.FILE_SEPARATOR);
			buf.append(pathParts[i]);
		}

		buf.append(VFSPath.FILE_SEPARATOR);
		buf.append(newFileName);

		return new VFSPathImpl(diskMgr, buf.toString());
	}
}
