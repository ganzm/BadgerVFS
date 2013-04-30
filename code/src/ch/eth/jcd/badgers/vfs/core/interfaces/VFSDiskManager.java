package ch.eth.jcd.badgers.vfs.core.interfaces;

import java.util.List;
import java.util.UUID;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
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
	 * Returns all journals which have not yet been transfered to the synchronization server (because we were offline some time)
	 * 
	 * @return sorted list of journals, the first element of the list is the oldest journal
	 * @throws VFSException
	 */
	List<Journal> getPendingJournals() throws VFSException;

	/**
	 * 
	 * @param hostName
	 * @throws VFSException
	 *             is thrown if this disk is already linked
	 * @return
	 */
	Journal linkDisk(String hostName) throws VFSException;

	/**
	 * we need journaling to be temporary disabled while replaying journals
	 * 
	 * @param pause
	 */
	void pauseJournaling(boolean pause);

	/**
	 * Returns the currents disks UUID
	 * 
	 * @return uuid of the disk
	 */
	UUID getDiskId();
}
