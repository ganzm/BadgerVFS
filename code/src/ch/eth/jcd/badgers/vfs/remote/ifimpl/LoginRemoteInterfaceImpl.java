package ch.eth.jcd.badgers.vfs.remote.ifimpl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.interfaces.LoginRemoteInterface;
import ch.eth.jcd.badgers.vfs.sync.server.ClientLink;
import ch.eth.jcd.badgers.vfs.sync.server.ServerConfiguration;
import ch.eth.jcd.badgers.vfs.sync.server.UserAccount;

public class LoginRemoteInterfaceImpl implements LoginRemoteInterface {

	private static final Logger LOGGER = Logger.getLogger(LoginRemoteInterfaceImpl.class);

	private final ServerConfiguration config;

	public LoginRemoteInterfaceImpl(final ServerConfiguration serverConfig) {
		this.config = serverConfig;
	}

	@Override
	public AdministrationRemoteInterface login(final String username, final String password) throws RemoteException, VFSException {
		LOGGER.info("login Username: " + username);

		final UserAccount userAccount = config.getUserAccount(username, password);
		final ClientLink clientLink = new ClientLink(userAccount);

		final AdministrationRemoteInterfaceImpl obj = new AdministrationRemoteInterfaceImpl(clientLink, config);
		final AdministrationRemoteInterface stub = (AdministrationRemoteInterface) UnicastRemoteObject.exportObject(obj, 0);

		return stub;
	}

	@Override
	public AdministrationRemoteInterface registerUser(final String username, final String password) throws RemoteException, VFSException {
		LOGGER.info("register Username: " + username);

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
		AdministrationRemoteInterfaceImpl remoteInterfaceImpl = (AdministrationRemoteInterfaceImpl) remoteInterface;
		LOGGER.info("logout Username: " + remoteInterfaceImpl.getClientLink().getUserAccount().getUsername());
		UnicastRemoteObject.unexportObject(remoteInterface, true);
	}

}
