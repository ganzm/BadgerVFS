package ch.eth.jcd.badgers.vfs.sync.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;

public class UserAccount implements Serializable {

	private static final long serialVersionUID = 1058529120906749114L;
	private final String username;
	private final String password;

	private final List<LinkedDisk> linkedDisks = new ArrayList<>();

	public UserAccount(final String username, final String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public String toString() {
		return "UserAccount " + username;
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
}
