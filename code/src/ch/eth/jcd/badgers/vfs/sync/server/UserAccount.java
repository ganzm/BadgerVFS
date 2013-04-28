package ch.eth.jcd.badgers.vfs.sync.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImplFactory;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DiskWorkerController;

public class UserAccount implements Serializable {

	private static final long serialVersionUID = 1058529120906749114L;
	private final String username;
	private final String password;

	private final List<LinkedDisk> linkedDisks = new ArrayList<>();
	private final Map<UUID, DiskWorkerController> activeDiskControllers = new HashMap<UUID, DiskWorkerController>();

	public UserAccount(final String username, final String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public List<LinkedDisk> getLinkedDisks() {
		return linkedDisks;
	}

	public void addLinkedDisk(final LinkedDisk linkedDisk) {
		linkedDisks.add(linkedDisk);
	}

	public LinkedDisk getLinkedDiskById(final UUID diskId) {
		for (final LinkedDisk disk : linkedDisks) {
			if (disk.getId().equals(diskId)) {
				return disk;
			}
		}
		return null;
	}

	public DiskWorkerController getDiskControllerForDiskWithId(final UUID diskId) throws VFSException {
		final LinkedDisk disk = getLinkedDiskById(diskId);
		if (disk != null) {
			DiskWorkerController retVal = activeDiskControllers.get(diskId);
			if (retVal == null || !retVal.isRunning()) {
				final VFSDiskManagerFactory factory = VFSDiskManagerImplFactory.getInstance();
				final VFSDiskManager diskManager = factory.openDiskManager(disk.getDiskConfig());
				retVal = new DiskWorkerController(diskManager);
				activeDiskControllers.put(diskId, retVal);
			}
			return retVal;
		}
		return null;
	}
}
