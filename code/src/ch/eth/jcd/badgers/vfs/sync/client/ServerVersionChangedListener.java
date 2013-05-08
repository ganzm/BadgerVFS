package ch.eth.jcd.badgers.vfs.sync.client;

import ch.eth.jcd.badgers.vfs.remote.interfaces.DiskRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.DiskRemoteResult;

/**
 * Listener is being notified whenever something has changed on the server
 * 
 * @see RemoteLongTermPoller
 * @see RemoteManager#setServerVersionChangedListener(ServerVersionChangedListener)
 * @see DiskRemoteInterface#longTermPollVersion(long)
 * 
 */
public interface ServerVersionChangedListener {

	void serverVersionChanged(DiskRemoteResult remoteResult);
}
