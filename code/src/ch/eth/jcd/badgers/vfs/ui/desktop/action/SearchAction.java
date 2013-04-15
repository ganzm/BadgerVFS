package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.FindInFolderCallback;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.model.SearchParameter;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class SearchAction extends BadgerAction implements FindInFolderCallback {

	private static final Logger LOGGER = Logger.getLogger(SearchAction.class);

	private final SearchParameter searchParameter;
	private final String searchFolder;

	private boolean canceling = false;

	/**
	 * current directory being searched
	 */
	private String currentDirectory;

	public SearchAction(SearchParameter searchParameter, String searchFolder) {
		this.searchParameter = searchParameter;
		this.searchFolder = searchFolder;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		VFSPath searchFolderPath = diskManager.createPath(searchFolder);
		VFSEntry searchFolder = searchFolderPath.getVFSEntry();

		searchFolder.findInFolder(searchParameter.getSearchString(), this);
	}

	public void tryCancelSearch() {
		canceling = true;
	}

	public boolean isCanceling() {
		return canceling;
	}

	@Override
	public void foundEntry(VFSPath path) {
		LOGGER.debug("Found Entry " + path.getAbsolutePath());

	}

	@Override
	public boolean stopSearch(VFSPath currentDirectory) {

		this.currentDirectory = currentDirectory.getAbsolutePath();

		return canceling;
	}

	public String getCurrentDirectoryBeingSearched() {
		return currentDirectory;
	}

}
