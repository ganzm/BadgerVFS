package ch.eth.jcd.badgers.vfs.remote.ifimpl;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImplFactory;
import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.core.model.Compression;
import ch.eth.jcd.badgers.vfs.core.model.Encryption;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.interfaces.DiskRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.ActiveClientLink;
import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;
import ch.eth.jcd.badgers.vfs.sync.server.ClientLink;
import ch.eth.jcd.badgers.vfs.sync.server.ServerConfiguration;
import ch.eth.jcd.badgers.vfs.sync.server.ServerRemoteInterfaceManager;
import ch.eth.jcd.badgers.vfs.sync.server.UserAccount;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DiskWorkerController;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.WorkLoadIndicatorPassive;

public class AdministrationRemoteInterfaceImpl implements AdministrationRemoteInterface {

	private final ClientLink clientLink;
	private final ServerRemoteInterfaceManager ifManager;
	private final ServerConfiguration config;
	private final UUID id;
	private static final Logger LOGGER = Logger.getLogger(AdministrationRemoteInterfaceImpl.class);

	public AdministrationRemoteInterfaceImpl(final ClientLink clientLink, final ServerRemoteInterfaceManager ifManager) {
		this.clientLink = clientLink;
		this.ifManager = ifManager;
		this.config = ifManager.getConfig();
		id = UUID.randomUUID();
	}

	@Override
	public List<LinkedDisk> listDisks() throws RemoteException {
		final UserAccount account = clientLink.getUserAccount();
		return account.getLinkedDisks();
	}

	@Override
	public DiskRemoteInterface linkNewDisk(final LinkedDisk linkedDisk, final Journal journal) throws RemoteException, VFSException {
		final UserAccount userAccout = this.clientLink.getUserAccount();

		// create server side DiskConfiguration
		final DiskConfiguration diskConfiguration = new DiskConfiguration();
		diskConfiguration.setCompressionAlgorithm(Compression.NONE);
		diskConfiguration.setEncryptionAlgorithm(Encryption.NONE);
		diskConfiguration.setHostFilePath(createDiskPathFromUUID(linkedDisk.getId()));
		diskConfiguration.setDiskId(linkedDisk.getId());
		diskConfiguration.setMaximumSize(-1);

		// override diskConfiguration
		linkedDisk.setDiskConfiguration(diskConfiguration);
		userAccout.addLinkedDisk(linkedDisk);
		config.persist();

		// Create Disk and DiskManager
		diskConfiguration.setSyncServerMode(true);

		final VFSDiskManagerFactory factory = VFSDiskManagerImplFactory.getInstance();
		final VFSDiskManagerImpl diskManager = (VFSDiskManagerImpl) factory.createDiskManager(diskConfiguration);

		diskManager.replayInitialJournal(journal);

		ActiveClientLink activeClientLink = ifManager.getActiveClientLink(this);
		final DiskWorkerController diskWorkerController = new DiskWorkerController(diskManager, new WorkLoadIndicatorPassive());
		activeClientLink.getClientLink().setDiskWorkerController(diskWorkerController);

		final DiskRemoteInterfaceImpl obj = new DiskRemoteInterfaceImpl(diskWorkerController);
		final DiskRemoteInterface stub = (DiskRemoteInterface) UnicastRemoteObject.exportObject(obj, 0);
		ifManager.addActiveDiskRemoteInterfaceImpls(obj.getId(), obj);

		return stub;
	}

	@Override
	public DiskRemoteInterface useLinkedDisk(final UUID diskId) throws RemoteException, VFSException {
		UserAccount account = clientLink.getUserAccount();

		// check the currently linked disks for this
		DiskWorkerController workerController = null;
		List<ActiveClientLink> clientLinks = ifManager.getActiveClientLinks();
		for (ActiveClientLink activeClientLink : clientLinks) {
			ClientLink clientLink = activeClientLink.getClientLink();
			UUID clientLinkDiskId = clientLink.getDiskId();
			if (diskId.equals(clientLinkDiskId)) {
				LOGGER.info("Disk " + diskId + " already instantiated");
				workerController = clientLink.getDiskWorkerController();
				break;
			}
		}

		if (workerController == null) {
			LinkedDisk linkedDisk = account.getLinkedDiskById(diskId);
			DiskConfiguration diskConfig = linkedDisk.getDiskConfig();

			// hack...
			diskConfig.setSyncServerMode(true);

			final VFSDiskManagerFactory factory = VFSDiskManagerImplFactory.getInstance();
			VFSDiskManager diskManager = factory.openDiskManager(diskConfig);
			workerController = new DiskWorkerController(diskManager, new WorkLoadIndicatorPassive());
		}

		clientLink.setDiskWorkerController(workerController);

		final DiskRemoteInterfaceImpl obj = new DiskRemoteInterfaceImpl(workerController);
		final DiskRemoteInterface stub = (DiskRemoteInterface) UnicastRemoteObject.exportObject(obj, 0);
		ifManager.addActiveDiskRemoteInterfaceImpls(obj.getId(), obj);

		return stub;
	}

	public void closeDisk(final DiskRemoteInterface diskRemoteInterface) throws RemoteException, VFSException {
		diskRemoteInterface.close();
		UUID diskId = diskRemoteInterface.getId();
		LOGGER.info("closing disk for user Username: " + clientLink.getUserAccount().getUsername());
		if (ifManager.getActiveDiskRemoteInterfaceImpls().containsKey(diskId)) {
			UnicastRemoteObject.unexportObject(ifManager.getActiveDiskRemoteInterfaceImpls().get(diskId), true);
			ifManager.getActiveDiskRemoteInterfaceImpls().remove(diskId);
		}
	}

	@Override
	public UUID createNewDisk(final String diskname) throws RemoteException, VFSException {
		UUID diskId = UUID.randomUUID();
		LOGGER.debug("Generate new DiskId " + diskId);

		final DiskConfiguration diskConfiguration = new DiskConfiguration();
		diskConfiguration.setCompressionAlgorithm(Compression.NONE);
		diskConfiguration.setEncryptionAlgorithm(Encryption.NONE);
		diskConfiguration.setMaximumSize(-1);
		diskConfiguration.setDiskId(diskId);
		diskConfiguration.setSyncServerMode(true);

		LinkedDisk linkedDisk = new LinkedDisk(diskId, diskname, diskConfiguration);
		diskConfiguration.setHostFilePath(createDiskPathFromUUID(diskId));

		// create and close Disk
		final VFSDiskManagerFactory factory = VFSDiskManagerFactory.getInstance();
		VFSDiskManager diskManager = factory.createDiskManager(diskConfiguration);
		diskManager.close();

		clientLink.getUserAccount().addLinkedDisk(linkedDisk);
		config.persist();

		return diskId;
	}

	public ClientLink getClientLink() {
		return clientLink;
	}

	private String createDiskPathFromUUID(final UUID diskId) {
		return config.getBfsFileFolder().getAbsolutePath() + File.separatorChar + diskId + ".bfs";
	}

	@Override
	public void closeLinkedDisk(final DiskRemoteInterface diskInterface) throws RemoteException, VFSException {
		closeDisk(diskInterface);
	}

	@Override
	public UUID getId() throws RemoteException {
		return id;
	}
}
