package server;

import interfaces.LoginService;
import interfaces.VFSService;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

public class LoginServiceImpl implements LoginService {
	private Registry registry;

	@Override
	public VFSService doLogin(final String user, final String password) {
		final UUID uuid = UUID.randomUUID();

		try {
			final VFSService obj = new VFSServiceImpl(this, uuid.toString());

			final VFSService stub = (VFSService) UnicastRemoteObject.exportObject(obj, 0);

			return stub;

		} catch (final RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public boolean disconnect(final VFSServiceImpl vfsServiceImpl) {
		try {
			UnicastRemoteObject.unexportObject(vfsServiceImpl, true);
		} catch (final NoSuchObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public Registry getRegistry() {
		return registry;
	}
}
