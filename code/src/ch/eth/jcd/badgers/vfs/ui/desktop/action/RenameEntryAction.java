package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;

public class RenameEntryAction extends BadgerAction {

	private final EntryUiModel entryModel;
	private final int editedRow;
	private final String newEntryName;

	public RenameEntryAction(ActionObserver actionObserver, EntryUiModel currentEditedValue, int editedRow, String newEntryName) {
		super(actionObserver);
		this.entryModel = currentEditedValue;
		this.editedRow = editedRow;
		this.newEntryName = newEntryName;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		entryModel.getEntry().renameTo(newEntryName);
	}

	public EntryUiModel getEntryModel() {
		return entryModel;
	}

	public int getEditedRowIndex() {
		return editedRow;
	}

	public String getNewName() {
		return newEntryName;
	}

}
