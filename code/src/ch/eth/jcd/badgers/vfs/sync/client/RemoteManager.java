package ch.eth.jcd.badgers.vfs.sync.client;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.interfaces.DiskRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.interfaces.LoginRemoteInterface;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.remote.CloseLinkedDiskAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.remote.ConnectAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.remote.CreateNewDiskAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.remote.GetRemoteLinkedDiskAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.remote.LinkNewDiskAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.remote.LoginAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.remote.LogoutAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.remote.RegisterUserAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.remote.UseLinkedDiskAction;
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
	private DiskRemoteInterface currentLinkedDiskRemoteInterface;

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

	public boolean startLogin(final String username, final String password, final ConnectionStateListener connectionStateListener) {
		if (status != ConnectionStatus.CONNECTED) {
			return false;
		}
		connectionStateListeners.add(connectionStateListener);
		final LoginAction loginAction = new LoginAction(this, this, username, password);
		remoteWorkerController.enqueue(loginAction);
		return true;
	}

	public boolean registerUser(final String username, final String password, final ConnectionStateListener connectionStateListener) {
		if (status != ConnectionStatus.CONNECTED) {
			return false;
		}
		connectionStateListeners.add(connectionStateListener);
		final RegisterUserAction createLoginAction = new RegisterUserAction(this, username, password);
		remoteWorkerController.enqueue(createLoginAction);
		return true;
	}

	public boolean startCreateNewDisk(final String diskname, final ActionObserver actionObserver) {
		if (status != ConnectionStatus.LOGGED_IN) {
			return false;
		}
		final CreateNewDiskAction newDiskAction = new CreateNewDiskAction(actionObserver, this, diskname);
		remoteWorkerController.enqueue(newDiskAction);
		return true;
	}

	public boolean startGetRemoteLinkedDisk(final String localDiskPath, final UUID diskId, final ActionObserver actionObserver) {
		if (status != ConnectionStatus.LOGGED_IN) {
			return false;
		}
		final GetRemoteLinkedDiskAction getRemoteDiskAction = new GetRemoteLinkedDiskAction(actionObserver, this, localDiskPath, diskId);
		remoteWorkerController.enqueue(getRemoteDiskAction);
		return true;
	}

	/**
	 * blocking call links a classic mode disk to a remote SynchronizationServer
	 * 
	 * @param diskConfig
	 * @param actionObserver
	 * @return
	 * @throws VFSException
	 */
	public boolean linkNewDisk(final DiskConfiguration diskConfig, final Journal journal, final ActionObserver actionObserver) throws VFSException {
		try {
			if (status != ConnectionStatus.LOGGED_IN) {
				return false;
			}

			final LinkNewDiskAction linkNewDiskAction = new LinkNewDiskAction(actionObserver, adminInterface, diskConfig, journal);
			remoteWorkerController.enqueueBlocking(linkNewDiskAction);
			return true;
		} catch (InterruptedException e) {
			throw new VFSException(e);
		}
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
			// TODO remove GUI Code
			SwingUtil.handleException(null, e);
		}
	}

	@Override
	public void onActionFinished(final AbstractBadgerAction action) {
		if (action instanceof ConnectAction) {
			final ConnectAction loginAction = (ConnectAction) action;
			loginInterface = loginAction.getLoginInterface();

			setStatus(ConnectionStatus.CONNECTED);
		} else if (action instanceof LoginAction) {
			final LoginAction loginAction = (LoginAction) action;

			this.adminInterface = loginAction.getAdminInterface();
			setStatus(ConnectionStatus.LOGGED_IN);

		} else if (action instanceof RegisterUserAction) {
			final RegisterUserAction regUserAction = (RegisterUserAction) action;
			this.adminInterface = regUserAction.getAdminInterface();
			this.setStatus(ConnectionStatus.LOGGED_IN);
		}

		else if (action instanceof UseLinkedDiskAction) {
			currentLinkedDiskRemoteInterface = ((UseLinkedDiskAction) action).getResult();
			this.setStatus(ConnectionStatus.DISK_MODE);
		} else if (action instanceof LogoutAction) {
			setStatus(ConnectionStatus.DISCONNECTED);
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

	public AdministrationRemoteInterface getAdminInterface() {
		return adminInterface;
	}

	public String getHostLink() {
		return hostLink;
	}

	public boolean startCloseDisk() {
		LOGGER.trace("Start closing Linked disk");
		if (status != ConnectionStatus.DISK_MODE) {
			LOGGER.trace("Cannot close linked disk, when remoteManager is not logged in");
			return false;
		}

		final CloseLinkedDiskAction action = new CloseLinkedDiskAction(this, this);
		remoteWorkerController.enqueue(action);
		return true;

	}

	public boolean startUseLinkedDisk(final UUID diskId) {
		LOGGER.trace("Start using linked disk");
		if (status != ConnectionStatus.LOGGED_IN) {
			LOGGER.trace("cannot Start using linked disk, when not logged in");
			return false;
		}
		final UseLinkedDiskAction useLinkedDiskAction = new UseLinkedDiskAction(this, this, diskId);
		remoteWorkerController.enqueue(useLinkedDiskAction);
		return true;
	}

	public boolean logout() {
		if (status != ConnectionStatus.CONNECTED || status != ConnectionStatus.LOGGED_IN || status != ConnectionStatus.DISK_MODE) {
			return false;
		}
		final LogoutAction action = new LogoutAction(this, this);
		remoteWorkerController.enqueue(action);
		return true;

	}

	public LoginRemoteInterface getLoginInterface() {
		return loginInterface;
	}

	public DiskRemoteInterface getCurrentLinkedDiskRemoteInterface() {
		return currentLinkedDiskRemoteInterface;
	}

	public void removeConnectionStateListener(final ConnectionStateListener connectionStateListener) {
		connectionStateListeners.remove(connectionStateListener);
	}
}
