package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.core.journaling.VFSJournaling;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.DiskAction;

/**
 * This DiskAction is performed on the SynchronisationServer. Triggered by a client which wants to update to the latest version.
 * 
 */
public class GetVersionDeltaAction extends DiskAction {

	private final long lastSeenServerVersion;

	private List<Journal> result;

	public GetVersionDeltaAction(final long lastSeenServerVersion, final long clientVersion, final ActionObserver actionObserver) {
		super(actionObserver);
		this.lastSeenServerVersion = lastSeenServerVersion;
	}

	@Override
	public void runDiskAction(final VFSDiskManager diskManager) throws VFSException {

		VFSJournaling serverJournaling = diskManager.getJournaling();

		result = serverJournaling.getJournalsSince(lastSeenServerVersion);
	}

	public List<Journal> getResult() {
		return result;
	}
}
