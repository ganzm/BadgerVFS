package ch.eth.jcd.badgers.vfs.core.interfaces;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.exception.VFSInvalidPathException;

public interface VFSDiskManager {

	/**
	 * close this Manager and its corresponding virtual disk
	 * 
	 * releases any ressource associated with this Manager
	 */
	public void close() throws VFSException;

	/**
	 * releases this Manager, deletes the file and any containing file
	 */
	public void dispose() throws VFSException;

	/**
	 * Remaining disk space
	 * 
	 * @return
	 */
	public long getFreeSpace();

	/** returns the root folder of our file system */
	public VFSEntry getRoot();

	/**
	 * creates a path object from a string
	 * 
	 * conventions are
	 * <ul>
	 * <li>Folders are separated by a forward slash '/'</li>
	 * <li>The root folder is denoted by a forward slash '/'</li>
	 * <li>A path to a file ends with anything except a forward slash '/'</li>
	 * <li>TODO restrict character set used for pathes and file names</li>
	 * </ul>
	 * 
	 * 
	 * @param path
	 * @return
	 * @throws VFSInvalidPathException
	 */
	public VFSPath CreatePath(String path) throws VFSException;

	/**
	 * return the DiskConfigutation
	 * 
	 * @return
	 * @throws VFSException
	 */
	public DiskConfiguration getDiskConfiguration() throws VFSException;

}
