package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

/**
 * Updates the local disk to the same version as the SyncServer
 * 
 * TODO describe PullRemoteChanges
 * 
 */
public class DownloadRemoteChangesAction extends DiskAction {

	public DownloadRemoteChangesAction(ActionObserver actionObserver) {
		super(actionObserver);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		// TODO Auto-generated method stub

	}

}
