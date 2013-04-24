package ch.eth.jcd.badgers.vfs.remote.ifimpl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.interfaces.LoginRemoteInterface;
import ch.eth.jcd.badgers.vfs.sync.server.ClientLink;
import ch.eth.jcd.badgers.vfs.sync.server.UserAccount;

public class LoginRemoteInterfaceImpl implements LoginRemoteInterface {

	@Override
	public AdministrationRemoteInterface login(String username, String password) throws RemoteException, VFSException {

		// TODO do the login
		UserAccount userAccount = new UserAccount(username, password);
		ClientLink clientLink = new ClientLink(userAccount);

		final AdministrationRemoteInterfaceImpl obj = new AdministrationRemoteInterfaceImpl(clientLink);
		final AdministrationRemoteInterface stub = (AdministrationRemoteInterface) UnicastRemoteObject.exportObject(obj, 0);

		return stub;
	}

	@Override
	public AdministrationRemoteInterface registerUser(String username, String password) throws RemoteException, VFSException {
		// TODO register the userlogin
		UserAccount userAccount = new UserAccount(username, password);
		ClientLink clientLink = new ClientLink(userAccount);

		final AdministrationRemoteInterfaceImpl obj = new AdministrationRemoteInterfaceImpl(clientLink);
		final AdministrationRemoteInterface stub = (AdministrationRemoteInterface) UnicastRemoteObject.exportObject(obj, 0);

		return stub;
	}

	@Override
	public void logout(AdministrationRemoteInterface remoteInterface) throws RemoteException, VFSException {
		UnicastRemoteObject.unexportObject(remoteInterface, true);
	}

}
