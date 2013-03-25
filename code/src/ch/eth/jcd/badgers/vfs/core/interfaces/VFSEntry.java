package ch.eth.jcd.badgers.vfs.core.interfaces;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * $Id
 * 
 * Represents a folder or directory which actually exists on the file system
 * 
 */
public interface VFSEntry {

	/**
	 * copies this Entry recursively (deep copy) to a new location
	 * 
	 * @param newLocation
	 */
	public void copyTo(VFSPath newLocation);

	/**
	 * lists the content of a directory <br>
	 * does only work on directories
	 * 
	 * @return
	 */
	public List<VFSEntry> getChildren();

	/**
	 * Use this to read from a file
	 * 
	 * @return
	 */
	public InputStream getInputStream();

	/**
	 * in case you want to write stuff to this entry
	 * 
	 * @param writeMode
	 *            TODO
	 * @return
	 */
	public OutputStream getOutputStream(int writeMode);

	/**
	 * 
	 * @return returns the path of this file or folder
	 */
	public VFSPath getPath();

	/**
	 * 
	 * @return returns true if the VFSEntry is a directory
	 */
	public boolean isDirectory();

	/**
	 * moves this file or folder to another location
	 * 
	 * @param path
	 */
	public void moveTo(VFSPath path);

	/**
	 * conveninence method
	 * 
	 * @see moveTo
	 * @param newName
	 */
	public void renameTo(String newName);

	/**
	 * deletes the file or folder this Entry represents
	 */
	public void delete();

}
