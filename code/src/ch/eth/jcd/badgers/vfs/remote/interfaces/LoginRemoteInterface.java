package ch.eth.jcd.badgers.vfs.remote.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ch.eth.jcd.badgers.vfs.exception.VFSException;

public interface LoginRemoteInterface extends Remote {

	public final String LOGIN_INTERFACE_KEY = "Login Interface";

	/**
	 * try to login with specific credentials
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws RemoteException
	 *             thrown when something with the networking is wrong
	 * @throws VFSException
	 *             if username password combination is unknown/wrong
	 * 
	 */
	AdministrationRemoteInterface login(String username, String password) throws RemoteException, VFSException;

	/**
	 * try to create a new user on the synchronisation server
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws RemoteException
	 *             thrown when something with the networking is wrong
	 * @throws VFSException
	 *             if username already exists
	 */
	AdministrationRemoteInterface registerUser(String username, String password) throws RemoteException, VFSException;

	/**
	 * terminates the connection to the sync server
	 * 
	 * @throws RemoteException
	 * @throws VFSException
	 */
	void logout(AdministrationRemoteInterface remoteInterface) throws RemoteException, VFSException;

}
