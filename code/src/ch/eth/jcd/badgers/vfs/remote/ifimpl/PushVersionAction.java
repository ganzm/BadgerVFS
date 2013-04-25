package ch.eth.jcd.badgers.vfs.remote.ifimpl;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.model.Journal;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.DiskAction;

public class PushVersionAction extends DiskAction {

	public PushVersionAction(final long lastSeenServerVersion, final Journal clientJournal, final ActionObserver actionObserver) {
		super(actionObserver);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void runDiskAction(final VFSDiskManager diskManager) throws VFSException {
		// TODO Auto-generated method stub

	}

	public Journal getResult() {
		// TODO Auto-generated method stub
		return null;
	}

}
