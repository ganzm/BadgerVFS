package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import java.util.ArrayList;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.ParentFolderEntryUiModel;

public class OpenFileInFolderAction extends BadgerAction {
	private final EntryUiModel entryModel;

	private List<EntryUiModel> entries = new ArrayList<EntryUiModel>();

	private VFSPath folderPath;

	private ParentFolderEntryUiModel parentFolderEntryUiModel = null;

	public OpenFileInFolderAction(ActionObserver actionObserver, EntryUiModel entryModel) {
		super(actionObserver);
		this.entryModel = entryModel;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		VFSEntry entry = entryModel.getEntry();
		// the folder we want to open
		VFSEntry folder = entry.getParent();
		VFSEntry parent = folder.getParent();
		if (folder != parent) {
			parentFolderEntryUiModel = new ParentFolderEntryUiModel(parent);
		}

		folderPath = folder.getPath();
		List<VFSEntry> childEntries = folder.getChildren();
		for (VFSEntry vfsEntry : childEntries) {
			entries.add(new EntryUiModel(vfsEntry, vfsEntry.isDirectory()));
		}
	}

	public VFSPath getFolderPath() {
		return folderPath;
	}

	public List<EntryUiModel> getEntries() {
		return entries;
	}

	public ParentFolderEntryUiModel getParentFolderEntryUiModel() {
		return parentFolderEntryUiModel;
	}

}
