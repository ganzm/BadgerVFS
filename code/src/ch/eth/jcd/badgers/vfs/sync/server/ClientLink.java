package ch.eth.jcd.badgers.vfs.sync.server;

import java.util.UUID;

import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DiskWorkerController;

/**
 * Represents a single connection to a client
 * 
 * Multiple ClientLink instances may share a UserAccount instance when a user is logged in on multiple machines
 * 
 */
public class ClientLink {
	private final UserAccount userAccount;
	private DiskWorkerController diskWorkerController = null;

	public ClientLink(UserAccount userAccount) {
		this.userAccount = userAccount;
	}

	public void setDiskWorkerController(DiskWorkerController diskWorkerController) {
		if (this.diskWorkerController != null && diskWorkerController != null) {
			throw new VFSRuntimeException("Tried to override DiskManager for " + userAccount);
		}

		this.diskWorkerController = diskWorkerController;
	}

	public UserAccount getUserAccount() {
		return userAccount;
	}

	public DiskWorkerController getDiskWorkerController() {
		return diskWorkerController;
	}

	/**
	 * 
	 * @return NULL or the DiskId which is currently bound to this Client
	 */
	public UUID getDiskId() {
		if (diskWorkerController != null) {
			return diskWorkerController.getDiskManager().getDiskId();
		}

		return null;
	}
}
