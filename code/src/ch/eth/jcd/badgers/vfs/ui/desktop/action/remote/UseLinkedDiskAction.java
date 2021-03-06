package ch.eth.jcd.badgers.vfs.ui.desktop.action.remote;

import java.rmi.RemoteException;
import java.util.UUID;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.DiskRemoteInterface;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class UseLinkedDiskAction extends RemoteAction {

	private static final Logger LOGGER = Logger.getLogger(UseLinkedDiskAction.class);
	private final UUID diskId;
	private DiskRemoteInterface result;
	private final RemoteManager remoteManager;

	public UseLinkedDiskAction(final ActionObserver actionObserver, final RemoteManager remoteManager, final UUID diskId) {
		super(actionObserver);
		this.remoteManager = remoteManager;
		this.diskId = diskId;
	}

	@Override
	public void runRemoteAction() throws VFSException {
		try {
			LOGGER.trace("trying to use linked disk: " + diskId);
			result = remoteManager.getAdminInterface().useLinkedDisk(diskId);
			LOGGER.trace("got linkedDiskInterface");

		} catch (final RemoteException e) {
			LOGGER.error("error while getting DiskRemoteInterface", e);
		}
	}

	public DiskRemoteInterface getResult() {
		return result;
	}
}
