package ch.eth.jcd.badgers.vfs.remote.model;

import java.io.Serializable;
import java.util.UUID;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;

public class LinkedDisk implements Serializable {
	private final UUID uuid;
	private final String displayName;
	private DiskConfiguration diskConfig = new DiskConfiguration();

	public LinkedDisk(final String displayName, final DiskConfiguration diskConfig) {
		this.uuid = UUID.randomUUID();
		this.displayName = displayName;
		this.diskConfig = diskConfig;
	}

	public LinkedDisk(final UUID uuid, final String displayName) {
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
