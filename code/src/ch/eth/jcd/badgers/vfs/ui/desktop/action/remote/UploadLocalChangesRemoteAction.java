package ch.eth.jcd.badgers.vfs.ui.desktop.action.remote;

import java.rmi.RemoteException;

import ch.eth.jcd.badgers.vfs.core.journaling.ClientVersion;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.model.PushVersionResult;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class UploadLocalChangesRemoteAction extends RemoteAction {
	/**
	 * 
	 */
	private final RemoteManager remoteManager;
	private final ClientVersion clientVersion;
	private PushVersionResult result;

	public UploadLocalChangesRemoteAction(RemoteManager remoteManager, ActionObserver actionObserver, ClientVersion clientVersion) {
		super(actionObserver);
		this.remoteManager = remoteManager;
		this.clientVersion = clientVersion;
	}

	@Override
	public void runRemoteAction() throws VFSException {
		try {
			result = this.remoteManager.getCurrentLinkedDiskRemoteInterface().pushVersion(clientVersion);
		} catch (RemoteException re) {
			throw new VFSException(re);
		}
	}

	public PushVersionResult getResult() {
		return result;
	}
}