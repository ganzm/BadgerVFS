package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LoginService extends Remote {
	static final String LOGIN_SERVICE_KEY = "Login Service";

	VFSService doLogin(String user, String password) throws RemoteException;

}
