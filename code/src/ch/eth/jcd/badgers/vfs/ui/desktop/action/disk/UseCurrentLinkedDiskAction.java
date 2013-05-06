package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class UseCurrentLinkedDiskAction extends DiskAction {

	private final ActionObserver actionObserver;

	private final RemoteManager manager;
	private DiskConfiguration diskConfig;

	public UseCurrentLinkedDiskAction(final ActionObserver actionObserver, final RemoteManager manager) {
		super(actionObserver);
		this.manager = manager;
		this.actionObserver = actionObserver;
	}

	@Override
	public void runDiskAction(final VFSDiskManager diskManager) throws VFSException {
		// String hostLink = manager.getHostLink();
		// Journal journal = diskManager.linkDisk(hostLink);
		diskConfig = diskManager.getDiskConfiguration();

		manager.useLinkedDisk(diskConfig.getDiskId(), actionObserver);
	}

	public DiskConfiguration getDiskConfiguration() {
		return diskConfig;
	}

}
