package ch.eth.jcd.badgers.vfs.remote.model;

import java.io.Serializable;

public class DiskRemoteResult implements Serializable {
	private static final long serialVersionUID = 537567482688747181L;
	private final long serverVersion;

	public DiskRemoteResult(long serverVersion) {
		this.serverVersion = serverVersion;
	}

	public long getServerVersion() {
		return serverVersion;
	}
}
