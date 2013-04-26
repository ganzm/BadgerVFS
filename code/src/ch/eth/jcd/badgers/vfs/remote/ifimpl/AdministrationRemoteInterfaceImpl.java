package ch.eth.jcd.badgers.vfs.remote.ifimpl;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.UUID;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.interfaces.DiskRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;
import ch.eth.jcd.badgers.vfs.remote.streaming.RemoteInputStream;
import ch.eth.jcd.badgers.vfs.sync.server.ClientLink;
import ch.eth.jcd.badgers.vfs.sync.server.ServerConfiguration;
import ch.eth.jcd.badgers.vfs.sync.server.UserAccount;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DiskWorkerController;

public class AdministrationRemoteInterfaceImpl implements AdministrationRemoteInterface {

	private final ClientLink clientLink;
	private final ServerConfiguration config;

	public AdministrationRemoteInterfaceImpl(final ClientLink clientLink, final ServerConfiguration config) {
		this.clientLink = clientLink;
		this.config = config;
	}

	@Override
	public List<LinkedDisk> listDisks() throws RemoteException {
		final UserAccount account = clientLink.getUserAccount();
		return account.getLinkedDisks();
	}

	@Override
	public DiskRemoteInterface linkNewDisk(final LinkedDisk linkedDisk, final RemoteInputStream diskFileContent) throws RemoteException {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public DiskRemoteInterface useLinkedDisk(final UUID diskId) throws RemoteException, VFSException {

		final DiskWorkerController diskWorkerController = clientLink.getUserAccount().getDiskControllerForDiskWithId(diskId);

		if (diskWorkerController == null) {
			throw new VFSException("Disk Id " + diskId + " not known for " + clientLink.getUserAccount().getUsername());
		}

		final DiskRemoteInterfaceImpl obj = new DiskRemoteInterfaceImpl(diskWorkerController);
		final DiskRemoteInterface stub = (DiskRemoteInterface) UnicastRemoteObject.exportObject(obj, 0);

		return stub;
	}

	public void closeDisk(final DiskRemoteInterface diskRemoteInterface) throws RemoteException, VFSException {
		((DiskRemoteInterfaceImpl) diskRemoteInterface).close();
		UnicastRemoteObject.unexportObject(diskRemoteInterface, true);
	}

	@Override
	public void createNewDisk(final LinkedDisk linkedDiskPrototype) throws RemoteException, VFSException {
		final DiskConfiguration diskConfig = linkedDiskPrototype.getDiskConfig();
		diskConfig.setHostFilePath(config.getBfsFileFolder().getAbsolutePath() + File.separatorChar + linkedDiskPrototype.getId());
		final VFSDiskManagerFactory factory = VFSDiskManagerFactory.getInstance();
		factory.createDiskManager(diskConfig);
		clientLink.getUserAccount().addLinkedDisk(linkedDiskPrototype);
		config.persist();
	}

	public ClientLink getClientLink() {
		return clientLink;
	}
}
