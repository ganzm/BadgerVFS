package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import java.util.UUID;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.model.DiskSpaceUsage;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class QueryDiskInfoAction extends DiskAction {
	private DiskSpaceUsage diskSpaceUsage;
	private UUID diskId;
	private long serverVersion;
	private String linkedHostname;

	public QueryDiskInfoAction(ActionObserver actionObserver) {
		super(actionObserver);
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		diskSpaceUsage = diskManager.getDiskSpaceUsage();

		diskId = diskManager.getDiskId();
		serverVersion = diskManager.getServerVersion();
		linkedHostname = diskManager.getDiskConfiguration().getLinkedHostName();
	}

	public DiskSpaceUsage getDiskSpaceUsage() {
		return diskSpaceUsage;
	}

	@Override
	public boolean needsToLockGui() {
		return false;
	}

	public UUID getDiskId() {
		return diskId;
	}

	public long getServerVersion() {
		return serverVersion;
	}

	public String getLinkedHostname() {
		return linkedHostname;
	}

}
