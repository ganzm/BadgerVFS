package ch.eth.jcd.badgers.vfs.remote.ifimpl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.interfaces.LoginRemoteInterface;
import ch.eth.jcd.badgers.vfs.sync.server.ClientLink;
import ch.eth.jcd.badgers.vfs.sync.server.ServerConfiguration;
import ch.eth.jcd.badgers.vfs.sync.server.UserAccount;

public class LoginRemoteInterfaceImpl implements LoginRemoteInterface {

	private final ServerConfiguration config;

	public LoginRemoteInterfaceImpl(final ServerConfiguration serverConfig) {
		this.config = serverConfig;
	}

	@Override
	public AdministrationRemoteInterface login(final String username, final String password) throws RemoteException, VFSException {

		final UserAccount userAccount = config.getUserAccount(username, password);
		final ClientLink clientLink = new ClientLink(userAccount);

		final AdministrationRemoteInterfaceImpl obj = new AdministrationRemoteInterfaceImpl(clientLink, config);
		final AdministrationRemoteInterface stub = (AdministrationRemoteInterface) UnicastRemoteObject.exportObject(obj, 0);

		return stub;
	}

	@Override
	public AdministrationRemoteInterface registerUser(final String username, final String password) throws RemoteException, VFSException {
		// TODO register the userlogin
		final UserAccount userAccount = new UserAccount(username, password);
		config.setUserAccount(userAccount);
		final ClientLink clientLink = new ClientLink(userAccount);

		final AdministrationRemoteInterfaceImpl obj = new AdministrationRemoteInterfaceImpl(clientLink, config);
		final AdministrationRemoteInterface stub = (AdministrationRemoteInterface) UnicastRemoteObject.exportObject(obj, 0);

		return stub;
	}

	@Override
	public void logout(final AdministrationRemoteInterface remoteInterface) throws RemoteException, VFSException {
		UnicastRemoteObject.unexportObject(remoteInterface, true);
	}

}
