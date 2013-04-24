package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.model.DiskSpaceUsage;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class QueryDiskSpaceAction extends DiskAction {
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
