package ch.eth.jcd.badgers.vfs.ui.desktop.action.remote;

import java.rmi.RemoteException;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class DownloadRemoteChangesRemoteAction extends RemoteAction {
	/**
	 * 
	 */
	private final RemoteManager remoteManager;
	private final long lastSeenServerVersion;
	private List<Journal> journals;

	public DownloadRemoteChangesRemoteAction(RemoteManager remoteManager, ActionObserver actionObserver, long lastSeenServerVersion) {
		super(actionObserver);
		this.remoteManager = remoteManager;
		this.lastSeenServerVersion = lastSeenServerVersion;
	}

	@Override
	public void runRemoteAction() throws VFSException {
		try {
			journals = remoteManager.getCurrentLinkedDiskRemoteInterface().getVersionDelta(lastSeenServerVersion);
		} catch (RemoteException re) {
			throw new VFSException(re);
		}
	}

	public List<Journal> getResult() {
		return journals;
	}
}