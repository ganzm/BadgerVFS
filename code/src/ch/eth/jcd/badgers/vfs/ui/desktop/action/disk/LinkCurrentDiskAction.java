package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class LinkCurrentDiskAction extends DiskAction {

	private final ActionObserver actionObserver;

	private final RemoteManager manager;

	public LinkCurrentDiskAction(final ActionObserver actionObserver, final RemoteManager manager) {
		super(actionObserver);
		this.manager = manager;
		this.actionObserver = actionObserver;
	}

	@Override
	public void runDiskAction(final VFSDiskManager diskManager) throws VFSException {
		diskManager.getDiskConfiguration().setLinkedHostName(manager.getHostLink());
		manager.startLinkNewDisk(diskManager.getDiskConfiguration(), actionObserver);
	}
}
