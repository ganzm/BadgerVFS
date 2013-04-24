package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.FindInFolderCallback;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.model.SearchParameter;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.SearchController;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;

public class SearchAction extends DiskAction implements FindInFolderCallback {

	private static final Logger LOGGER = Logger.getLogger(SearchAction.class);

	private final SearchController searchController;
	private final SearchParameter searchParameter;
	private final String searchFolder;

	private boolean canceling = false;

	/**
	 * current directory being searched
	 */
	private String currentDirectory;

	public SearchAction(final SearchController searchController, final SearchParameter searchParameter, final String searchFolder) {
		super(searchController);
		this.searchParameter = searchParameter;
		this.searchFolder = searchFolder;
		this.searchController = searchController;
	}

	@Override
	public void runDiskAction(final VFSDiskManager diskManager) throws VFSException {
		final VFSPath searchFolderPath = diskManager.createPath(this.searchFolder);
		final VFSEntry searchFolder = searchFolderPath.getVFSEntry();

		if (searchFolder == null) {
			throw new VFSException("Invalid Path " + this.searchFolder);
		}
		searchFolder.findInFolder(searchParameter, this);
	}

	public void tryCancelSearch() {
		canceling = true;
	}

	public boolean isCanceling() {
		return canceling;
	}

	@Override
	public void foundEntry(final VFSPath path) {
		LOGGER.debug("Found Entry " + path.getAbsolutePath());

		try {
			final VFSEntry entry = path.getVFSEntry();
			final EntryUiModel entryModel = new EntryUiModel(entry, entry.isDirectory());

			// forward to our controller
			searchController.foundEntry(entryModel);
		} catch (final VFSException e) {
			LOGGER.error("Internal Error", e);
		}
	}

	@Override
	public boolean stopSearch(final VFSPath currentDirectory) {
		this.currentDirectory = currentDirectory.getAbsolutePath();
		return canceling;
	}

	public String getCurrentDirectoryBeingSearched() {
		return currentDirectory;
	}

	@Override
	public boolean needsToLockGui() {
		return false;
	}
}
