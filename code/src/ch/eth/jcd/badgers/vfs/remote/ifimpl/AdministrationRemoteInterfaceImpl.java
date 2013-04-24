package ch.eth.jcd.badgers.vfs.remote.ifimpl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.UUID;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.interfaces.DiskRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;
import ch.eth.jcd.badgers.vfs.remote.model.RemoteInputStream;
import ch.eth.jcd.badgers.vfs.sync.server.ClientLink;
import ch.eth.jcd.badgers.vfs.sync.server.UserAccount;

public class AdministrationRemoteInterfaceImpl implements AdministrationRemoteInterface {

	private final ClientLink clientLink;

	public AdministrationRemoteInterfaceImpl(ClientLink clientLink) {
		this.clientLink = clientLink;
	}

	@Override
	public List<LinkedDisk> listDisks() throws RemoteException {
		UserAccount account = clientLink.getUserAccount();
		return account.getLinkedDisks();
	}

	@Override
	public DiskRemoteInterface linkNewDisk(LinkedDisk linkedDisk, RemoteInputStream diskFileContent) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DiskRemoteInterface useLinkedDisk(UUID diskId) throws RemoteException, VFSException {

		LinkedDisk disk = clientLink.getUserAccount().getLinkedDiskById(diskId);

		if (disk == null) {
			throw new VFSException("Disk Id " + disk + " not known for " + clientLink.getUserAccount().getUsername());
		}

		final DiskRemoteInterfaceImpl obj = new DiskRemoteInterfaceImpl(clientLink, disk);
		final DiskRemoteInterface stub = (DiskRemoteInterface) UnicastRemoteObject.exportObject(obj, 0);

		return stub;
	}

	public void closeDisk(DiskRemoteInterface diskRemoteInterface) throws RemoteException, VFSException {
		((DiskRemoteInterfaceImpl) diskRemoteInterface).close();
		UnicastRemoteObject.unexportObject(diskRemoteInterface, true);
	}
}
