package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class GetServerVersionAction extends DiskAction {
	private long serverVersion;

	public GetServerVersionAction() {
		super(null);
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		serverVersion = diskManager.getServerVersion();
	}

	public long getServerVersion() {
		return serverVersion;
	}
}
