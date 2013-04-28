package ch.eth.jcd.badgers.vfs.ui.desktop.action.remote;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class CloseLinkedDiskAction extends RemoteAction {

	private static final Logger LOGGER = Logger.getLogger(CloseLinkedDiskAction.class);
	private final RemoteManager remoteManager;

	public CloseLinkedDiskAction(final ActionObserver actionObserver, final RemoteManager remoteManager) {
		super(actionObserver);
		this.remoteManager = remoteManager;
	}

	@Override
	public void runRemoteAction() throws VFSException {
		try {
			LOGGER.trace("trying to close disk...");
			remoteManager.getAdminInterface().closeLinkedDisk(remoteManager.getCurrentLinkedDiskRemoteInterface());
			LOGGER.trace("successfully closed disk!");
		} catch (final RemoteException e) {
			LOGGER.error("Error closing linked Disk", e);
		}

	}

}
