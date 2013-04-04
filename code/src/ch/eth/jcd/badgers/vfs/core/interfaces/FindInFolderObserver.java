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
	void foundEntry(VFSPath path);

	/**
	 * This method informs the "find"-implementation, whether the find shall be stopped. This method could for instance return false, if on the GUI a
	 * "STOP-Search" button was clicked. To inform the Observer on which directory the search is in exactly this moment the "find"-implementation should pass
	 * this directory via the currentDirectory parameter.
	 * 
	 * The "find"-implementation should check this method several times during search.
	 * 
	 * @param currentDirectory
	 * @return true if this search shall be stopped
	 */
	boolean stopSearch(VFSPath currentDirectory);
}
