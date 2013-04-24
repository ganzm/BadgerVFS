package ch.eth.jcd.badgers.vfs.remote.model;

import java.util.UUID;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;

public class LinkedDisk {
	private final UUID uuid;
	private String displayName;
	private DiskConfiguration diskConfig = new DiskConfiguration();

	public LinkedDisk(UUID uuid, String displayName) {
		this.uuid = uuid;
		this.displayName = displayName;
	}

	public UUID getId() {
		return uuid;
	}

	public String getDisplayName() {
		return displayName;
	}

	public DiskConfiguration getDiskConfig() {
		return diskConfig;
	}
}
