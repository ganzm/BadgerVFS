package ch.eth.jcd.badgers.vfs.core.journaling;

import java.io.Serializable;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class ClientVersion implements Serializable {

	private static final long serialVersionUID = -4810096622012249541L;

	/**
	 * This is the Version last seen on the synchronization server
	 * 
	 * up to this version there are no differences from the local to the server side version
	 * 
	 */
	private final long serverVersion;

	private List<Journal> journals;

	public ClientVersion(long serverVersion) {
		this.serverVersion = serverVersion;
	}

	public long getServerVersion() {
		return serverVersion;
	}

	public List<Journal> getJournals() {
		return journals;
	}

	public void setJournals(List<Journal> journals) {
		this.journals = journals;
	}

	public void beforeRmiTransport(VFSDiskManager diskManager) throws VFSException {
		for (Journal journal : journals) {
			journal.beforeRmiTransport(diskManager);
		}
	}

	public boolean isEmpty() {
		return journals == null || journals.isEmpty();
	}
}
