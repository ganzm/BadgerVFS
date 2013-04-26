package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import streaming.RemoteInputStream;
import streaming.RemoteOutputStream;

public interface VFSService extends Remote {
	boolean createFile(String fileName) throws RemoteException;

	boolean disconnect() throws RemoteException;

	RemoteOutputStream getOutputStream(String fileName) throws RemoteException;

	RemoteInputStream getInputStream(String fileName) throws RemoteException;

	boolean uploadFile(RemoteInputStream inputStream, String filePath) throws RemoteException;

	boolean downloadFile(final RemoteOutputStream outputStream, final String fileString) throws RemoteException;

}
