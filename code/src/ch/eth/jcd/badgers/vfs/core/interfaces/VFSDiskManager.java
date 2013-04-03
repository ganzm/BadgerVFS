package ch.eth.jcd.badgers.vfs.core.interfaces;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public interface VFSDiskManager {

	/**
	 * close this Manager and its corresponding virtual disk
	 * 
	 * releases any ressource associated with this Manager
	 */
	void close() throws VFSException;

	/**
	 * releases this Manager, deletes the file and any containing file
	 */
	void dispose() throws VFSException;

	/**
	 * Remaining disk space
	 * 
	 * @return
	 */
	long getFreeSpace();

	/** returns the root folder of our file system */
	VFSEntry getRoot();

	/**
	 * return the DiskConfigutation
	 * 
	 * @return
	 * @throws VFSException
	 */
	DiskConfiguration getDiskConfiguration() throws VFSException;

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
	 * @throws VFSException
	 *             (VFSInvalidPathException)
	 */
	VFSPath createPath(String path) throws VFSException;

}
