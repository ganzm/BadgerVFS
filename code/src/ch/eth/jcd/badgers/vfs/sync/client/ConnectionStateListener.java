package ch.eth.jcd.badgers.vfs.sync.client;

public interface ConnectionStateListener {

	void connectionStateChanged(ConnectionStatus status);
}
