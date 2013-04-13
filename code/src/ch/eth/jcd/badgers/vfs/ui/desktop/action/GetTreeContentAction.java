package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import java.util.ArrayList;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiTreeModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiTreeNode;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.ParentFolderEntryUiModel;

public class GetTreeContentAction extends BadgerAction {

	private List<EntryUiModel> uiEntries;

	/**
	 * folder for which we want to load entries
	 */
	private EntryUiTreeNode folderEntryModel;

	/**
	 * 
	 * @param folderEntryModel
	 *            folder for which we want to load entries
	 */
	public GetTreeContentAction(EntryUiTreeNode folderEntryModel) {
		this.folderEntryModel = folderEntryModel;
	}

	/**
	 * Use this constructor to load from root directory
	 */
	public GetTreeContentAction() {
		this.folderEntryModel = null;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {

		VFSEntry folder;
		if (folderEntryModel == null) {
			folder = diskManager.getRoot();
		} else {
			folder = folderEntryModel.getUiEntry().getEntry();
		}

		List<VFSEntry> entries = folder.getChildren();
		uiEntries = new ArrayList<EntryUiModel>();
		for (VFSEntry entry : entries) {
			if(entry.isDirectory())
				uiEntries.add(new EntryUiModel(entry, entry.isDirectory()));
		}
	}

	public List<EntryUiModel> getEntries() {
		return uiEntries;
	}
	
	public EntryUiTreeNode getParent(){
		return folderEntryModel;
	}

	public String getFolderPath() {
		if(folderEntryModel == null){
			return "/";
		}
		return folderEntryModel.getUiEntry().getFullPath();
	}

}
