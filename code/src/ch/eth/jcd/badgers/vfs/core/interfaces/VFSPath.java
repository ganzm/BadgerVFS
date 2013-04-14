package ch.eth.jcd.badgers.vfs.core.interfaces;

import ch.eth.jcd.badgers.vfs.exception.VFSException;

/**
 * $Id$
 * 
 * Path to a {@link VFSEntry} contrary to {@link VFSEntry} a {@link VFSPath} does not need to exist on the file system
 */
public interface VFSPath {

	String FILE_SEPARATOR = "/";

	VFSEntry createDirectory() throws VFSException;

	VFSEntry createFile() throws VFSException;

	/**
	 * check whether a path actually exists or not
	 * 
	 * @return
	 * @throws VFSException
	 */
	boolean exists() throws VFSException;

	/**
	 * @return returns the complete path of this current object as string
	 * @throws VFSException
	 */
	String getAbsolutePath();

	String getParentPath() throws VFSException;

	/**
	 * may be implemented as getAbsolutePath().subString(getAbsolutePath().lastIndexOf(VFSPath.FILE_SEPARATOR), ...);
	 * 
	 * @return returns the file or directory name represented by this path
	 * 
	 * @throws VFSException
	 */
	String getName();

	VFSEntry getVFSEntry() throws VFSException;
}
