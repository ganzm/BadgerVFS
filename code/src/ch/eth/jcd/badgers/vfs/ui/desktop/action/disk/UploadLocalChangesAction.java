package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.journaling.ClientVersion;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.model.PushVersionResult;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

/**
 * gemäss Upload Pull SequenzDiagramm
 * 
 * Versucht 1x lokale Änderungen auf den SynchServer hochzuladen
 * 
 * 
 * If this action fails
 * 
 * 
 */
public class UploadLocalChangesAction extends DiskAction {

	private final RemoteManager manager;
	private static final Logger LOGGER = Logger.getLogger(UploadLocalChangesAction.class);

	public UploadLocalChangesAction(ActionObserver actionObserver, final RemoteManager manager) {
		super(actionObserver);
		this.manager = manager;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		ClientVersion clientVersion = diskManager.getPendingVersion();
		clientVersion.beforeRmiTransport(diskManager);
		try {
			PushVersionResult result = manager.getCurrentLinkedDiskRemoteInterface().pushVersion(clientVersion);
			diskManager.setSynchronized(result.getNewServerVersion());
		} catch (final RemoteException e) {
			LOGGER.error(e);
			throw new VFSException(e);
		}
	}

}
