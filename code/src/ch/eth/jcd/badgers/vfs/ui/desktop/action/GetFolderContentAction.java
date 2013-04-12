package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import java.util.ArrayList;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.ParentFolderEntryUiModel;

public class GetFolderContentAction extends BadgerAction {

	private List<EntryUiModel> uiEntries;

	private ParentFolderEntryUiModel parentFolder;

	/**
	 * folder for which we want to load entries
	 */
	private EntryUiModel folderEntryModel;

	/**
	 * 
	 * @param folderEntryModel
	 *            folder for which we want to load entries
	 */
	public GetFolderContentAction(EntryUiModel folderEntryModel) {
		this.folderEntryModel = folderEntryModel;
	}

	/**
	 * Use this constructor to load from root directory
	 */
	public GetFolderContentAction() {
		this.folderEntryModel = null;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {

		VFSEntry folder;
		if (folderEntryModel == null) {
			folder = diskManager.getRoot();
			folderEntryModel = new EntryUiModel(folder, true);
		} else {
			folder = folderEntryModel.getEntry();
		}

		VFSEntry parentEntry = folder.getParent();

		if (parentEntry != folder) {
			// we are not at the root
			parentFolder = new ParentFolderEntryUiModel(folder.getParent());
		}

		List<VFSEntry> entries = folder.getChildren();
		uiEntries = new ArrayList<EntryUiModel>();
		for (VFSEntry entry : entries) {
			uiEntries.add(new EntryUiModel(entry, entry.isDirectory()));
		}
	}

	public List<EntryUiModel> getEntries() {
		return uiEntries;
	}

	public String getFolderPath() {
		return folderEntryModel.getFullPath();
	}

	public ParentFolderEntryUiModel getParentFolderEntryModel() {
		return parentFolder;
	}
}
