package ch.eth.jcd.badgers.vfs.sync.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;

public class UserAccount {
	private final String username;
	private final String password;

	private List<LinkedDisk> linkedDisks = new ArrayList<>();

	public UserAccount(String username, String password) {
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

	public LinkedDisk getLinkedDiskById(UUID diskId) {
		for (LinkedDisk disk : linkedDisks) {
			if (disk.getId().equals(diskId)) {
				return disk;
			}
		}
		return null;
	}
}
