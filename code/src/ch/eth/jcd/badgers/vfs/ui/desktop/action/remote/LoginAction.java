package ch.eth.jcd.badgers.vfs.ui.desktop.action.remote;

import java.rmi.RemoteException;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.LoginRemoteInterface;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class LoginAction extends RemoteAction {
	private final LoginRemoteInterface loginInterface;
	private final String username;
	private final String password;

	public LoginAction(ActionObserver actionObserver, LoginRemoteInterface loginInterface, String username, String password) {
		super(actionObserver);
		this.loginInterface = loginInterface;
		this.username = username;
		this.password = password;
	}

	@Override
	public void runRemoteAction() throws VFSException {
		try {
			loginInterface.login(username, password);
		} catch (RemoteException e) {
			throw new VFSException(e);
		}
	}
}
