package ch.eth.jcd.badgers.vfs.remote.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

import ch.eth.jcd.badgers.vfs.core.journaling.ClientVersion;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.model.DiskRemoteResult;
import ch.eth.jcd.badgers.vfs.remote.model.PushVersionResult;

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
	 * Whenever you do that you should call {@link #downloadFinished()}
	 * 
	 * @return
	 */
	List<Journal> getVersionDelta(long lastSeenServerVersion) throws RemoteException;

	/**
	 * Releases DownloadStreams on the Synchronization Server
	 * 
	 * @see {@link #getVersionDelta(long, long)}
	 */
	void downloadFinished() throws RemoteException;

	/**
	 * pushes locally made changes to the server. The client needs to be ready to revert his changes because an other client may have made conflicting changes
	 * 
	 * @param clientVersion
	 * @throws RemoteException
	 * @return
	 */
	PushVersionResult pushVersion(ClientVersion clientVersion) throws RemoteException;

	/**
	 * Deleted the currently managed disk on the server. Any other client which currently uses this this is disconnected
	 * 
	 * 
	 * Invalidates this RemoteInterface
	 * 
	 * @throws RemoteException
	 */
	void unlink() throws RemoteException;

	/**
	 * Closes the DiskRemoteInterface and deregisters it from RMI
	 * 
	 * @throws RemoteException
	 * @throws VFSException
	 */
	void close() throws RemoteException, VFSException;

	/**
	 * 
	 * @return UUID from the Impl
	 * @throws RemoteException
	 */
	UUID getId() throws RemoteException;

}
