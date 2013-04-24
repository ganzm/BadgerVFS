package ch.eth.jcd.badgers.vfs.remote.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import ch.eth.jcd.badgers.vfs.remote.model.DiskRemoteResult;
import ch.eth.jcd.badgers.vfs.remote.model.Journal;

/**
 * 
 * This interface is used to perform actions related to a single linked disk
 * 
 */
public interface DiskRemoteInterface extends Remote {

	/**
	 * Long Poll method
	 * 
	 * This method should be called by the client in a separate thread
	 * 
	 * The server blocks this method as long as the clientVersion is equal to the serverVersion. As soon as there is any changes made to the current disk this
	 * method return. The client may then get the new journal files and update its local disk.
	 * 
	 * @return
	 */
	DiskRemoteResult longTermPollVersion(long clientVersion) throws RemoteException;

	/**
	 * Asks the server for new data
	 * 
	 * @param clientVersion
	 * @return
	 */
	List<Journal> getVersionDelta(long lastSeenServerVersion, long clientVersion) throws RemoteException;

	/**
	 * pushes locally made changes to the server. The client needs to be ready to revert his changes because an other client may have made conflicting changes
	 * 
	 * @param clientJournal
	 * @throws RemoteException
	 * @return slightly modified journal which was created from the server
	 */
	Journal pushVersion(long lastSeenServerVersion, Journal clientJournal) throws RemoteException;

	/**
	 * Invalidates this RemoteInterface
	 * 
	 * 
	 * Closes the connection to the client
	 * 
	 * @throws RemoteException
	 */
	void close() throws RemoteException;

	/**
	 * Deleted the currently managed disk on the server.
	 * 
	 * 
	 * Invalidates this RemoteInterface
	 * 
	 * @throws RemoteException
	 */
	void unlink() throws RemoteException;

}
