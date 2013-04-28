package ch.eth.jcd.badgers.vfs.ui.desktop.action.remote;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class LogoutAction extends RemoteAction {

	private static final Logger LOGGER = Logger.getLogger(LogoutAction.class);
	private final RemoteManager remoteManager;

	public LogoutAction(final ActionObserver actionObserver, final RemoteManager remoteManager) {
		super(actionObserver);
		this.remoteManager = remoteManager;
	}

	@Override
	public void runRemoteAction() throws VFSException {
		try {
			LOGGER.trace("Client trying to log out...");
			remoteManager.getLoginInterface().logout(remoteManager.getAdminInterface());
			LOGGER.trace("Client successfully logged out");
		} catch (final RemoteException e) {
			LOGGER.error("Error on logout: ", e);
		}
	}

}
