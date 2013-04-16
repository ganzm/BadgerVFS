package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;

public class DeleteEntryAction extends BadgerAction {

	private final List<EntryUiModel> entries;

	public DeleteEntryAction(ActionObserver actionObserver, List<EntryUiModel> entries) {
		super(actionObserver);
		this.entries = entries;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		for (EntryUiModel uiEntry : entries) {
			uiEntry.getEntry().delete();
		}
	}
}
