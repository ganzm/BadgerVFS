package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.journaling.ClientVersion;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.core.journaling.PathConflict;
import ch.eth.jcd.badgers.vfs.core.journaling.VFSJournaling;
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

	private static final Logger LOGGER = Logger.getLogger(DownloadRemoteChangesAction.class);

	private final RemoteManager remoteManager;

	public DownloadRemoteChangesAction(ActionObserver actionObserver, final RemoteManager manager) {
		super(actionObserver);
		this.remoteManager = manager;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		long lastSeenServerVersion = diskManager.getServerVersion();

		List<Journal> journals = revertLocalChanges(diskManager);
		try {
			// apply changes from server
			List<Journal> toUpdate = remoteManager.getVersionDelta(lastSeenServerVersion);
			for (Journal j : toUpdate) {
				try {
					j.replay(diskManager);
				} catch (VFSException e) {
					// yes this exception handling is not ideal, but there is nothing what we can do when update process fails
					LOGGER.error("Error while replaying Journal " + j.getJournalFolderName(), e);
				} finally {
					diskManager.setServerVersion(diskManager.getServerVersion() + 1);
				}
			}

			redoLocalChanges(diskManager, journals);

		} finally {
			remoteManager.downloadFinished();
		}
	}

	public static void redoLocalChanges(VFSDiskManager diskManager, List<Journal> journals) throws VFSException {
		VFSJournaling journaling = diskManager.getJournaling();

		// redo local changes, but resolve conflicts
		List<PathConflict> conflicts = new ArrayList<>();
		for (Journal journal : journals) {
			journal.replayResolveConflics(diskManager, ".client1", conflicts);

			// override journal
			journaling.overrideJournal(journal);
		}
	}

	public static List<Journal> revertLocalChanges(VFSDiskManager diskManager) throws VFSException {
		// revert local changes, but does not touch the corresponding journals
		ClientVersion version = diskManager.getPendingVersion();
		List<Journal> journals = version.getJournals();
		for (int i = journals.size() - 1; i >= 0; i--) {
			Journal journal = journals.get(i);
			journal.revert(diskManager);
		}
		return journals;
	}

}
