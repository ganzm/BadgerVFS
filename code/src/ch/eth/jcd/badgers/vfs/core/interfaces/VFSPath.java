package ch.eth.jcd.badgers.vfs.core.interfaces;

import ch.eth.jcd.badgers.vfs.exception.VFSException;

/**
 * $Id$
 * 
 * Path to a {@link VFSEntry} contrary to {@link VFSEntry} a {@link VFSPath} does not need to exist on the file system
 */
public interface VFSPath {

	public static final String FILE_SEPARATOR = "/";

	public VFSEntry createDirectory() throws VFSException;

	public VFSEntry createFile() throws VFSException;

	public boolean exists() throws VFSException;

	/**
	 * @return returns the complete path of this current object as string
	 * @throws VFSException
	 */
	public String getAbsolutePath() throws VFSException;

	/**
	 * may be implemented as getAbsolutePath().subString(getAbsolutePath().lastIndexOf(VFSPath.FILE_SEPARATOR), ...);
	 * 
	 * @return returns the file or directory name represented by this path
	 * 
	 * @throws VFSException
	 */
	public String getName() throws VFSException;

	public VFSEntry getVFSEntry() throws VFSException;
}
