package ch.eth.jcd.badgers.vfs.remote.ifimpl;

import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.journaling.ClientVersion;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.core.journaling.VFSJournaling;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.model.PushVersionResult;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.DiskAction;

/**
 * This Action is performed on the SynchronisationServer whenever a client tries to publish his new version
 * 
 * 
 */
public class SyncServerPushVersionAction extends DiskAction {
	private static final Logger LOGGER = Logger.getLogger(SyncServerPushVersionAction.class);

	private final ClientVersion clientVersion;
	private PushVersionResult result;

	public SyncServerPushVersionAction(ClientVersion clientVersion) {
		super(null);
		this.clientVersion = clientVersion;
	}

	@Override
	public void runDiskAction(final VFSDiskManager diskManager) throws VFSException {

		long serverVersion = diskManager.getServerVersion();
		if (clientVersion.getServerVersion() != serverVersion) {
			// force client to update

			result = new PushVersionResult(false, "Client has Version " + clientVersion.getServerVersion() + " but current ServerVersion is " + serverVersion
					+ " - perform an update on the client first");
			return;
		}

		VFSJournaling journaling = diskManager.getJournaling();

		try {
			List<Journal> journals = clientVersion.getJournals();
			for (Journal journal : journals) {
				journaling.openNewJournal(false);
				journal.replay(diskManager);
				diskManager.persistServerJournal(journal);
			}

			long newServerVersion = diskManager.getServerVersion();
			result = new PushVersionResult(true, newServerVersion);
		} catch (VFSException ex) {
			LOGGER.error("Error while pushing Version", ex);
			result = new PushVersionResult(false, ex.getClass().getName() + " - " + ex.getMessage());
		}
	}

	public PushVersionResult getResult() {
		return result;
	}

}
