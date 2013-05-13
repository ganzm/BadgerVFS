package ch.eth.jcd.badgers.vfs.remote.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;

public interface AdministrationRemoteInterface extends Remote {

	/**
	 * Returns all Disk currently managed by the server for the user which is currently logged in
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
	DiskRemoteInterface linkNewDisk(LinkedDisk linkedDisk, Journal journal) throws RemoteException, VFSException;

	/**
	 * This method is called from the client whenever a linked disk was opened or when the client wants to use and copy an existing disk located on the server
	 * 
	 * @param linkedDisk
	 * @return
	 * @throws RemoteException
	 */
	DiskRemoteInterface useLinkedDisk(UUID diskId) throws RemoteException, VFSException;

	/**
	 * This method is called from the client whenever on the server-side a new disk shall be created.
	 * 
	 * @param linkedDiskPrototype
	 * @return
	 * @throws RemoteException
	 * @throws VFSException
	 * @return UUID of the freshly created disk
	 */
	UUID createNewDisk(String diskname) throws RemoteException, VFSException;

	/**
	 * The client must tell the server when he closes a currently opened disk. This is done via this method
	 * 
	 * @param diskInterface
	 * @param diskId
	 * @throws RemoteException
	 * @throws VFSException
	 */
	void closeLinkedDisk(DiskRemoteInterface diskInterface) throws RemoteException, VFSException;

	/**
	 * 
	 * @return UUID from the Impl
	 * @throws RemoteException
	 */
	UUID getId() throws RemoteException;
}
