package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

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

	private final RemoteManager remoteManager;
	private static final Logger LOGGER = Logger.getLogger(UploadLocalChangesAction.class);

	public UploadLocalChangesAction(ActionObserver actionObserver, final RemoteManager manager) {
		super(actionObserver);
		this.remoteManager = manager;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		LOGGER.debug("Starting upload action on remote Manager");
		ClientVersion clientVersion = diskManager.getPendingVersion();

		if (clientVersion.isEmpty()) {
			LOGGER.debug("Nothing to upload");
			return;
		}

		clientVersion.beforeRmiTransport(diskManager);
		PushVersionResult result = remoteManager.pushVersion(clientVersion);
		diskManager.setSynchronized(result.getNewServerVersion());
		LOGGER.debug("Finished upload action on remote Manager");
	}

}
