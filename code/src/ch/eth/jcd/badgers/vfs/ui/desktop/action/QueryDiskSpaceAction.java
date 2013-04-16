package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class QueryDiskSpaceAction extends BadgerAction {
	private long freeSpace;
	private long maxSpace;

	public QueryDiskSpaceAction(ActionObserver actionObserver) {
		super(actionObserver);
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		freeSpace = diskManager.getFreeSpace();
		maxSpace = diskManager.getMaxSpace();
	}

	public long getFreeSpace() {
		return freeSpace;
	}

	public long getMaxSpace() {
		return maxSpace;
	}
}
