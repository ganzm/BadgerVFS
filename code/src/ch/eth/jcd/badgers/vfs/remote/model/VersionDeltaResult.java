package ch.eth.jcd.badgers.vfs.remote.model;

import java.io.Serializable;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.journaling.Journal;

public class VersionDeltaResult implements Serializable {

	private static final long serialVersionUID = 6149134591775126976L;

	private List<Journal> journals;
	private long serverVersion;

	public List<Journal> getJournals() {
		return journals;
	}

	public void setJournals(List<Journal> journals) {
		this.journals = journals;
	}

	public long getServerVersion() {
		return serverVersion;
	}

	public void setServerVersion(long serverVersion) {
		this.serverVersion = serverVersion;
	}
}
