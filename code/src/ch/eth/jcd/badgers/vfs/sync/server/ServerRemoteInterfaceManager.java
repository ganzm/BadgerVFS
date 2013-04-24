package ch.eth.jcd.badgers.vfs.sync.server;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.ifimpl.LoginRemoteInterfaceImpl;
import ch.eth.jcd.badgers.vfs.remote.interfaces.LoginRemoteInterface;

public class ServerRemoteInterfaceManager {
	private static final Logger LOGGER = Logger.getLogger(ServerRemoteInterfaceManager.class);

	public void setup() throws VFSException {
		try {
			final LoginRemoteInterfaceImpl obj = new LoginRemoteInterfaceImpl();
			final LoginRemoteInterface stub = (LoginRemoteInterface) UnicastRemoteObject.exportObject(obj, 0);

			// Bind the remote object's stub in the registry.
			LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			final Registry registry = LocateRegistry.getRegistry();
			registry.bind(LoginRemoteInterface.LOGIN_INTERFACE_KEY, stub);

			LOGGER.info("Server ready on port " + Registry.REGISTRY_PORT);
		} catch (final AlreadyBoundException | RemoteException e) {
			throw new VFSException(e);
		}
	}
}
