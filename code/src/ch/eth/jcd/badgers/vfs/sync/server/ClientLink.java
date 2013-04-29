package ch.eth.jcd.badgers.vfs.sync.server;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;

/**
 * Represents a single connection to a client
 * 
 * 
 * Multiple ClientLink instances may share a UserAccount instance when a user is logged in on multiple machines
 * 
 */
public class ClientLink {

	private final UserAccount userAccount;
	private VFSDiskManager diskManager = null;

	public ClientLink(UserAccount userAccount) {
		this.userAccount = userAccount;
	}

	public UserAccount getUserAccount() {
		return userAccount;
	}

	public void setDiskManager(VFSDiskManager diskManager) {
		if (this.diskManager != null && diskManager != null) {
			throw new VFSRuntimeException("Tried to override DiskManager for " + userAccount);
		}

		this.diskManager = diskManager;
	}
}
