package ch.eth.jcd.badgers.vfs.remote.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;
import ch.eth.rmi.streaming.RemoteInputStream;

public interface AdministrationRemoteInterface extends Remote {

	/**
	 * Returns all Disk currently managed by the server for the user which is currently loged in
	 * 
	 * @return
	 */
	List<LinkedDisk> listDisks() throws RemoteException;

	/**
	 * The client calls this method whenever he wants to link a classic disk to a synchronisation server.
	 * 
	 * The disk on the server is created without size constraints and without compression (speed)
	 * 
	 * @param guid
	 * @param diskName
	 * @return
	 * @throws RemoteException
	 */
	DiskRemoteInterface linkNewDisk(LinkedDisk linkedDisk, RemoteInputStream diskFileContent) throws RemoteException;

	/**
	 * This method is called from the client whenever a linked disk was opened or when the client wants to use and copy an existing disk located on the server
	 * 
	 * @param linkedDisk
	 * @return
	 * @throws RemoteException
	 */
	DiskRemoteInterface useLinkedDisk(LinkedDisk linkedDisk) throws RemoteException;

}
