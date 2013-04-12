package ch.eth.jcd.badgers.vfs.ui.desktop.model;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;

public class ParentFolderEntryUiModel extends EntryUiModel {

	public ParentFolderEntryUiModel(VFSEntry entry) {
		super(entry, true);
	}

	public ParentFolderEntryUiModel(EntryUiModel entryModel) {
		super(entryModel.getEntry(), true);
	}

	@Override
	public String getDisplayName() {
		return "..";
	}

}
