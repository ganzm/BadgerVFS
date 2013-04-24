package ch.eth.jcd.badgers.vfs.sync.client;

/**
 * $ID$
 * 
 * Convenience class
 * 
 * so we don't have to check for null if there is no remote link on the ui
 * 
 */
public class OfflineRemoteManager extends RemoteManager {

	public OfflineRemoteManager() {
		super(null);
	}
}
