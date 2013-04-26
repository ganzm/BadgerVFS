package server;

import interfaces.LoginService;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {

	public static void main(final String args[]) {

		try {
			final LoginService obj = new LoginServiceImpl();
			final LoginService stub = (LoginService) UnicastRemoteObject.exportObject(obj, 0);

			// Bind the remote object's stub in the registry.
			LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			final Registry registry = LocateRegistry.getRegistry();
			registry.bind(LoginService.LOGIN_SERVICE_KEY, stub);

			System.err.println("Server ready");
		} catch (final AlreadyBoundException | RemoteException e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}
}
