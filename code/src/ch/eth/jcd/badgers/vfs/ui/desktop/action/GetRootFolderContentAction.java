package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import java.util.ArrayList;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;

public class GetRootFolderContentAction extends BadgerAction {

	private List<EntryUiModel> rootEntries;

	public List<EntryUiModel> getRootEntries() {
		return rootEntries;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {

		VFSEntry rootEntry = diskManager.getRoot();

		List<VFSEntry> entries = rootEntry.getChildren();

		rootEntries = new ArrayList<EntryUiModel>();
		for (VFSEntry entry : entries) {
			rootEntries.add(new EntryUiModel(entry));
		}
	}
}
