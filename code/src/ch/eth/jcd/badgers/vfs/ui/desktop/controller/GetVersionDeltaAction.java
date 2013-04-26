package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.DiskAction;

public class GetVersionDeltaAction extends DiskAction {

	public GetVersionDeltaAction(final long lastSeenServerVersion, final long clientVersion, final ActionObserver actionObserver) {
		super(actionObserver);
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void runDiskAction(final VFSDiskManager diskManager) throws VFSException {
		// TODO Auto-generated method stub

	}

	public List<Journal> getResult() {
		// TODO Auto-generated method stub
		return null;
	}

}
