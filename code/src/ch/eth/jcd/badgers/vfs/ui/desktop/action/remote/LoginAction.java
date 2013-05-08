package ch.eth.jcd.badgers.vfs.ui.desktop.action.remote;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class LoginAction extends RemoteAction {
	private static final Logger LOGGER = Logger.getLogger(LoginAction.class);
	private final RemoteManager remoteManager;
	private final String username;
	private final String password;
	private AdministrationRemoteInterface adminInterface;

	public LoginAction(final ActionObserver actionObserver, final RemoteManager remoteManager, final String username, final String password) {
		super(actionObserver);
		this.remoteManager = remoteManager;
		this.username = username;
		this.password = password;
	}

	@Override
	public void runRemoteAction() throws VFSException {
		try {
			LOGGER.info("Login with Username " + username);
			adminInterface = remoteManager.getLoginInterface().login(username, password);
		} catch (final RemoteException e) {
			throw new VFSException(e);
		}
	}

	public AdministrationRemoteInterface getAdminInterface() {
		return adminInterface;
	}
}
