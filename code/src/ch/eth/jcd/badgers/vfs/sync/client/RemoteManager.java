package ch.eth.jcd.badgers.vfs.sync.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.interfaces.LoginRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.remote.ConnectAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.remote.CreateNewDiskAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.remote.LoginAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.remote.RegisterUserAction;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

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
	private AdministrationRemoteInterface adminInterface;

	private final List<ConnectionStateListener> connectionStateListeners = new ArrayList<>();

	public RemoteManager(final String hostLink) {
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

	public boolean startLogin(final String username, final String password) {
		return startLogin(username, password, this);
	}

	public boolean startLogin(final String username, final String password, final ActionObserver observer) {
		if (status != ConnectionStatus.CONNECTED) {
			return false;
		}
		final LoginAction loginAction = new LoginAction(observer, loginInterface, username, password);
		remoteWorkerController.enqueue(loginAction);
		return true;
	}

	public boolean registerUser(final String username, final String password, final ActionObserver observer) {
		if (status != ConnectionStatus.CONNECTED) {
			return false;
		}
		final RegisterUserAction createLoginAction = new RegisterUserAction(observer, loginInterface, username, password);
		remoteWorkerController.enqueue(createLoginAction);
		return true;
	}

	public boolean startCreateNewDisk(final LinkedDisk prototype, final ActionObserver actionObserver) {
		if (status != ConnectionStatus.CONNECTED) {
			return false;
		}
		final CreateNewDiskAction newDiskAction = new CreateNewDiskAction(actionObserver, adminInterface, prototype);
		remoteWorkerController.enqueue(newDiskAction);
		return true;
	}

	public ConnectionStatus getConnectionStatus() {
		return status;
	}

	public void dispose() {
		remoteWorkerController.dispose();
	}

	@Override
	public void onActionFailed(final AbstractBadgerAction action, final Exception e) {
		if (action instanceof ConnectAction) {
			setStatus(ConnectionStatus.DISCONNECTED);
		} else {
			SwingUtil.handleException(null, e);
		}
	}

	@Override
	public void onActionFinished(final AbstractBadgerAction action) {
		if (action instanceof ConnectAction) {
			final ConnectAction loginAction = (ConnectAction) action;
			loginInterface = loginAction.getLoginInterface();

			setStatus(ConnectionStatus.CONNECTED);
		}
		if (action instanceof LoginAction) {
			final LoginAction loginAction = (LoginAction) action;

		}
	}

	private void setStatus(final ConnectionStatus status) {
		if (this.status != status) {
			LOGGER.info("Switch ConnectionStatus To " + status + " was  " + this.status);
			this.status = status;

			// notify listeners
			for (final ConnectionStateListener csl : connectionStateListeners) {
				try {
					csl.connectionStateChanged(status);
				} catch (final RuntimeException ex) {
					LOGGER.error("", ex);
				}
			}

		}
	}

	public void addConnectionStateListener(final ConnectionStateListener connectionStateListener) {
		connectionStateListeners.add(connectionStateListener);
	}

	public void setAdminInterface(final AdministrationRemoteInterface adminInterface2) {
		this.adminInterface = adminInterface2;
	}

	public AdministrationRemoteInterface getAdminInterface() {
		return adminInterface;
	}

}
