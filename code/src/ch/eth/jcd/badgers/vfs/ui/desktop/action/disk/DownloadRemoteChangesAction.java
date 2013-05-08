package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

/**
 * Updates the local disk to the same version as the SyncServer
 * 
 * This action is performed on the client side
 * 
 * 
 */
public class DownloadRemoteChangesAction extends DiskAction {

	private final RemoteManager remoteManager;

	public DownloadRemoteChangesAction(ActionObserver actionObserver, final RemoteManager manager) {
		super(actionObserver);
		this.remoteManager = manager;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		long lastSeenServerVersion = diskManager.getServerVersion();
		List<Journal> toUpdate;

		try {
			toUpdate = remoteManager.getVersionDelta(lastSeenServerVersion);
			for (Journal j : toUpdate) {
				j.replay(diskManager);
				diskManager.setServerVersion(diskManager.getServerVersion() + 1);
			}
		} finally {
			remoteManager.downloadFinished();
		}
	}

}
