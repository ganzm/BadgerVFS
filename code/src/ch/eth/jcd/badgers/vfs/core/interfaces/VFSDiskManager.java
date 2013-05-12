package ch.eth.jcd.badgers.vfs.core.interfaces;

import java.util.UUID;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.journaling.ClientVersion;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.core.journaling.VFSJournaling;
import ch.eth.jcd.badgers.vfs.core.model.DiskSpaceUsage;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public interface VFSDiskManager {

	/**
	 * close this Manager and its corresponding virtual disk
	 * 
	 * releases any resource associated with this Manager
	 */
	void close() throws VFSException;

	/**
	 * releases this Manager, deletes the file and any containing file
	 */
	void dispose() throws VFSException;

	/**
	 * Remaining disk space on the available for user data
	 * 
	 * @return
	 */
	long getFreeSpace() throws VFSException;

	/**
	 * Total disk space available for user data
	 * 
	 * @return
	 * @throws VFSException
	 */
	long getMaxSpace() throws VFSException;

	/**
	 * get a summary about disk space used
	 */
	DiskSpaceUsage getDiskSpaceUsage() throws VFSException;

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

	/**
	 * search for files or folders with a specific name on the whole virtual disk
	 * 
	 * Whenever a file is found the observer callback is called. Further search is blocked by the call to the observer
	 * 
	 * @see VFSEntry#findInFolder(String, FindInFolderCallback)
	 * 
	 * @param fileName
	 * @param observer
	 */
	void find(String fileName, FindInFolderCallback observer) throws VFSException;

	/**
	 * puts all operations performed since the last call of this operation into a Journal file and persists it on the disk
	 * 
	 * @return
	 * @throws VFSException
	 */
	void closeCurrentJournal() throws VFSException;

	/**
	 * Returns the current version which has not yet been published to the server
	 * 
	 * 
	 * @return sorted list of journals, the first element of the list is the oldest journal
	 * @throws VFSException
	 */
	ClientVersion getPendingVersion() throws VFSException;

	/**
	 * Returns the most current version of the synchronisation server
	 * 
	 * @return
	 */
	long getServerVersion();

	/**
	 * Sets the current version
	 * 
	 * @param serverVersion
	 * @throws VFSException
	 */
	void setServerVersion(long serverVersion) throws VFSException;

	/**
	 * 
	 * @param hostName
	 * @throws VFSException
	 *             is thrown if this disk is already linked
	 * @return
	 */
	Journal linkDisk(String hostName) throws VFSException;

	/**
	 * Returns the currents disks UUID
	 * 
	 * @return uuid of the disk
	 */
	UUID getDiskId();

	VFSJournaling getJournaling();

	/**
	 * This method is called by a client whenever local changes where successfully pushed to the synchronization server
	 * 
	 * @param newServerVersion
	 */
	void setSynchronized(long newServerVersion) throws VFSException;

}
