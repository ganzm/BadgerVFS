package ch.eth.jcd.badgers.vfs.sync.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.LoginRemoteInterface;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.remote.ConnectAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.remote.LoginAction;

/**
 * Client side class which encapsulates all remote access and disk linking operations
 * 
 */
public class RemoteManager implements ActionObserver {

	private static final Logger LOGGER = Logger.getLogger(RemoteManager.class);

	private final RemoteWorkerController remoteWorkerController;
	private final String hostLink;
	private ConnectionStatus status = ConnectionStatus.DISCONNECTED;

	private LoginRemoteInterface loginInterface;

	private List<ConnectionStateListener> connectionStateListeners = new ArrayList<>();

	public RemoteManager(String hostLink) {
		this.hostLink = hostLink;
		this.remoteWorkerController = new RemoteWorkerController();
	}

	/**
	 * nonblocking starts and tries to connect to the remote host
	 */
	public void start() {
		if (status != ConnectionStatus.DISCONNECTED) {
			throw new VFSRuntimeException("Cannot start while beeing " + status);
		}

		setStatus(ConnectionStatus.CONNECTING);
		remoteWorkerController.startWorkerController();
		remoteWorkerController.enqueue(new ConnectAction(hostLink, this));
	}

	public boolean startLogin(String username, String password) {
		if (status != ConnectionStatus.CONNECTED) {
			return false;
		}
		LoginAction loginAction = new LoginAction(this, loginInterface, username, password);
		remoteWorkerController.enqueue(loginAction);
		return true;
	}

	public ConnectionStatus getConnectionStatus() {
		return status;
	}

	public void dispose() {
		remoteWorkerController.dispose();
	}

	@Override
	public void onActionFailed(AbstractBadgerAction action, Exception e) {
		if (action instanceof ConnectAction) {
			setStatus(ConnectionStatus.DISCONNECTED);
		}
	}

	@Override
	public void onActionFinished(AbstractBadgerAction action) {
		if (action instanceof ConnectAction) {
			setStatus(ConnectionStatus.CONNECTED);

			ConnectAction loginAction = (ConnectAction) action;
			loginInterface = loginAction.getLoginInterface();
		}
	}

	private void setStatus(ConnectionStatus status) {
		if (this.status != status) {
			LOGGER.info("Switch ConnectionStatus To " + status + " was  " + this.status);
			this.status = status;

			// notify listeners
			for (ConnectionStateListener csl : connectionStateListeners) {
				try {
					csl.connectionStateChanged(status);
				} catch (RuntimeException ex) {
					LOGGER.error("", ex);
				}
			}

		}
	}

	public void addConnectionStateListener(ConnectionStateListener connectionStateListener) {
		connectionStateListeners.add(connectionStateListener);
	}
}
