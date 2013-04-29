package ch.eth.jcd.badgers.vfs.ui.desktop.action.remote;

import java.rmi.RemoteException;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class CreateNewDiskAction extends RemoteAction {

	private final RemoteManager remoteManager;
	private final String diskname;

	public CreateNewDiskAction(final ActionObserver actionObserver, final RemoteManager remoteManager, final String diskname) {
		super(actionObserver);
		this.diskname = diskname;
		this.remoteManager = remoteManager;
	}

	@Override
	public void runRemoteAction() throws VFSException {
		try {
			remoteManager.getAdminInterface().createNewDisk(diskname);
		} catch (final RemoteException e) {
			throw new VFSException(e);
		}
	}

}
