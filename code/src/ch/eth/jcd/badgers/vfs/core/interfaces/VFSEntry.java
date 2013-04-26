package ch.eth.jcd.badgers.vfs.core.interfaces;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.model.SearchParameter;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

/**
 * $Id$
 * 
 * Represents a folder or directory which actually exists on the file system
 * 
 */
public interface VFSEntry {

	/**
	 * File write mode
	 * 
	 * Overwrites existing data
	 * 
	 * @see VFSEntry#getOutputStream(int)
	 */
	int WRITE_MODE_OVERRIDE = 0;

	/**
	 * copies this Entry recursively (deep copy) to a new location
	 * 
	 * Sample Usage: /home/user/mat copyTo /home/user/mat_copy
	 * 
	 * @param newLocation
	 *            path inclusive name of the entry which is being copied
	 */
	void copyTo(VFSPath newLocation) throws VFSException;

	/**
	 * lists the content of a directory <br>
	 * does only work on directories
	 * 
	 * @return
	 */
	List<VFSEntry> getChildren() throws VFSException;

	/**
	 * Use this to read from a file
	 * 
	 * @return
	 */
	InputStream getInputStream() throws VFSException;

	/**
	 * in case you want to write stuff to this entry
	 * 
	 * @see VFSEntry#WRITE_MODE_OVERRIDE
	 * @param writeMode
	 *            TODO
	 * @return
	 */
	OutputStream getOutputStream(int writeMode) throws VFSException;

	/**
	 * 
	 * @return returns the path of this file or folder
	 */
	VFSPath getPath();

	/**
	 * This Method creates a fully initialized new VFSPath object with <code> this (VFSEntry)</code> as parent and <code>childName</code> as child. With this
	 * method no client of the filesystem framework needs to create new VFSPaths or VFSEntries itself.
	 * 
	 * @return returns a fully initialized new VFSPath object which <code> this (VFSEntry)</code> as parent and <code>childName</code> as child
	 */
	VFSPath getChildPath(String childName) throws VFSException;

	/**
	 * returns the parent of current entry. if root returns itself
	 * 
	 * @return
	 * @throws VFSException
	 */
	VFSEntry getParent() throws VFSException;

	/**
	 * 
	 * @return returns true if the VFSEntry is a directory
	 */
	boolean isDirectory();

	/**
	 * moves this file or folder to another location
	 * 
	 * @param path
	 */
	void moveTo(VFSPath path) throws VFSException;

	/**
	 * convenience method
	 * 
	 * @see moveTo
	 * @param newName
	 */
	void renameTo(String newName) throws VFSException;

	/**
	 * deletes the file or folder this Entry represents
	 * 
	 * throws an exception if you try to delete a non empty directory
	 * 
	 * This instance is marked as deleted. Any further method call will lead to an exception
	 * 
	 */
	void delete() throws VFSException;

	/**
	 * search for files or folders with a specific name in the current Entry and all its subfolders
	 * 
	 * Whenever a file is found the observer callback is called. Further search is blocked by the call to the observer
	 * 
	 * convenience method for
	 * 
	 * @see this{@link #findInFolder(SearchParameter, FindInFolderCallback)}
	 * @param fileName
	 * @param observer
	 */
	void findInFolder(String fileName, FindInFolderCallback observer) throws VFSException;

	/**
	 * search for file or folders
	 * 
	 * @param searchParameter
	 *            contains search string and sear parameters
	 * 
	 * @param observer
	 *            callback which receives events when an entry was found
	 * @throws VFSException
	 */
	void findInFolder(SearchParameter searchParameter, FindInFolderCallback observer) throws VFSException;

}
