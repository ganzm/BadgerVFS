package ch.eth.jcd.badgers.vfs.core.interfaces;

public interface VFSDiskManager {

	/**
	 * close this Manager and its corresponding virtual disk
	 * 
	 * releases any ressource associated with this Manager
	 */
	public void close();

	/**
	 * releases this Manager, deletes the file and any containing file
	 */
	public void dispose();

	/**
	 * Remaining disk space
	 * 
	 * @return
	 */
	public long getFreeSpace();

	/** returns the root folder of our file system */
	public VFSEntry getRoot();

}
