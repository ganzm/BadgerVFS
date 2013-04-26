package ch.eth.jcd.badgers.vfs.ui.desktop.action.remote;

import java.rmi.RemoteException;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.interfaces.LoginRemoteInterface;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class LoginAction extends RemoteAction {
	private final LoginRemoteInterface loginInterface;
	private final String username;
	private final String password;
	private AdministrationRemoteInterface adminInterface;

	public LoginAction(final ActionObserver actionObserver, final LoginRemoteInterface loginInterface, final String username, final String password) {
		super(actionObserver);
		this.loginInterface = loginInterface;
		this.username = username;
		this.password = password;
	}

	@Override
	public void runRemoteAction() throws VFSException {
		try {
			adminInterface = loginInterface.login(username, password);
		} catch (final RemoteException e) {
			throw new VFSException(e);
		}
	}

	public AdministrationRemoteInterface getAdminInterface() {
		return adminInterface;
	}
}
