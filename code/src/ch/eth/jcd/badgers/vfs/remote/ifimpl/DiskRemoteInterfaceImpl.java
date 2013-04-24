package ch.eth.jcd.badgers.vfs.remote.ifimpl;

import java.rmi.RemoteException;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImplFactory;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.DiskRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.DiskRemoteResult;
import ch.eth.jcd.badgers.vfs.remote.model.Journal;
import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;
import ch.eth.jcd.badgers.vfs.sync.server.ClientLink;

public class DiskRemoteInterfaceImpl implements DiskRemoteInterface {

	private final ClientLink clientLink;
	private final LinkedDisk linkedDisk;

	private VFSDiskManager diskManager;

	public DiskRemoteInterfaceImpl(ClientLink clientLink, LinkedDisk linkedDisk) throws VFSException {
		this.clientLink = clientLink;
		this.linkedDisk = linkedDisk;

		VFSDiskManagerFactory factory = VFSDiskManagerImplFactory.getInstance();
		diskManager = factory.createDiskManager(linkedDisk.getDiskConfig());
	}

	@Override
	public DiskRemoteResult longTermPollVersion(long clientVersion) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Journal> getVersionDelta(long lastSeenServerVersion, long clientVersion) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Journal pushVersion(long lastSeenServerVersion, Journal clientJournal) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public void close() throws RemoteException, VFSException {
		diskManager.close();
	}

	@Override
	public void unlink() throws RemoteException {
		// TODO Auto-generated method stub

	}

}
