package ch.eth.jcd.badgers.vfs.remote.ifimpl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.interfaces.LoginRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.ActiveClientLink;
import ch.eth.jcd.badgers.vfs.sync.server.ClientLink;
import ch.eth.jcd.badgers.vfs.sync.server.ServerConfiguration;
import ch.eth.jcd.badgers.vfs.sync.server.ServerRemoteInterfaceManager;
import ch.eth.jcd.badgers.vfs.sync.server.UserAccount;

public class LoginRemoteInterfaceImpl implements LoginRemoteInterface {

	private static final Logger LOGGER = Logger.getLogger(LoginRemoteInterfaceImpl.class);

	private final ServerRemoteInterfaceManager ifManager;

	public LoginRemoteInterfaceImpl(final ServerRemoteInterfaceManager ifManager) {
		this.ifManager = ifManager;
	}

	@Override
	public synchronized AdministrationRemoteInterface login(final String username, final String password) throws RemoteException, VFSException {
		LOGGER.info("login Username: " + username);

		ServerConfiguration config = ifManager.getConfig();
		final UserAccount userAccount = config.getUserAccount(username, password);

		final AdministrationRemoteInterface stub = createAdminRemoteInterface(userAccount);

		return stub;
	}

	@Override
	public synchronized AdministrationRemoteInterface registerUser(final String username, final String password) throws RemoteException, VFSException {
		LOGGER.info("register Username: " + username);

		ServerConfiguration config = ifManager.getConfig();
		final UserAccount userAccount = new UserAccount(username, password);

		config.setUserAccount(userAccount);
		config.persist();

		final AdministrationRemoteInterface stub = createAdminRemoteInterface(userAccount);

		return stub;
	}

	private AdministrationRemoteInterface createAdminRemoteInterface(final UserAccount userAccount) throws RemoteException {
		final ClientLink clientLink = new ClientLink(userAccount);
		ActiveClientLink activeLink = new ActiveClientLink(clientLink);
		ifManager.addActiveClientLink(activeLink);
		final AdministrationRemoteInterfaceImpl obj = new AdministrationRemoteInterfaceImpl(clientLink, ifManager);
		final AdministrationRemoteInterface stub = (AdministrationRemoteInterface) UnicastRemoteObject.exportObject(obj, 0);

		activeLink.setRmiIf(obj, stub);
		return stub;
	}

	@Override
	public synchronized void logout(final AdministrationRemoteInterface remoteInterface) throws RemoteException, VFSException {
		for (ActiveClientLink activeLink : ifManager.getActiveClientLinks()) {
			if (activeLink.getRemoteIf().getId().equals(remoteInterface.getId())) {
				LOGGER.info("logout Username: " + activeLink.getClientLink().getUserAccount().getUsername());
				UnicastRemoteObject.unexportObject(activeLink.getRemoteifImpl(), true);
				ifManager.getActiveClientLinks().remove(activeLink);
				break;
			}
		}
	}
}
