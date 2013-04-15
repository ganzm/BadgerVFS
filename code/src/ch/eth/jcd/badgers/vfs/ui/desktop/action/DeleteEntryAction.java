package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;

public class DeleteEntryAction extends BadgerAction {

	private final EntryUiModel entry;
	private final int rowIndexToDelete;

	public DeleteEntryAction(ActionObserver actionObserver, EntryUiModel entry, int rowIndexToDelete) {
		super(actionObserver);
		this.entry = entry;
		this.rowIndexToDelete = rowIndexToDelete;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		entry.getEntry().delete();
	}

	public EntryUiModel getEntryToRemove() {
		return entry;
	}

	public int getRowIndexToRemove() {
		return rowIndexToDelete;
	}
}
