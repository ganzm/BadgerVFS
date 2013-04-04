package ch.eth.jcd.badgers.vfs.core.interfaces;

/**
 * @see VFSEntry#findInFolder(String)
 * 
 * 
 * 
 */
public interface FindInFolderObserver {

	/**
	 * This method is called from the VFSDiskManager whenever it finds a file
	 * 
	 * @param entry
	 *            entry which matches
	 */
	public void foundEntry(VFSEntry entry);
}
