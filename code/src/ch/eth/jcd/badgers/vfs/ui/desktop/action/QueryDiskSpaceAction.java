package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.model.DiskSpaceUsage;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class QueryDiskSpaceAction extends BadgerAction {
	private DiskSpaceUsage diskSpaceUsage;

	public QueryDiskSpaceAction(ActionObserver actionObserver) {
		super(actionObserver);
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		diskSpaceUsage = diskManager.getDiskSpaceUsage();
	}

	public DiskSpaceUsage getDiskSpaceUsage() {
		return diskSpaceUsage;
	}

	@Override
	public boolean needsToLockGui() {
		return false;
	}

}
