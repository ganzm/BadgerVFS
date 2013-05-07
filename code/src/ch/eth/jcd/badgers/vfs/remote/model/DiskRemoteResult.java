package ch.eth.jcd.badgers.vfs.remote.model;

public class DiskRemoteResult {
	private final long serverVersion;

	public DiskRemoteResult(long serverVersion) {
		this.serverVersion = serverVersion;
	}

	public long getServerVersion() {
		return serverVersion;
	}
}
