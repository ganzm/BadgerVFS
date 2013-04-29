package ch.eth.jcd.badgers.vfs.remote.ifimpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImplFactory;
import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.core.model.Compression;
import ch.eth.jcd.badgers.vfs.core.model.Encryption;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.interfaces.DiskRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;
import ch.eth.jcd.badgers.vfs.remote.streaming.RemoteInputStream;
import ch.eth.jcd.badgers.vfs.remote.streaming.RemoteOutputStream;
import ch.eth.jcd.badgers.vfs.sync.server.ClientLink;
import ch.eth.jcd.badgers.vfs.sync.server.ServerConfiguration;
import ch.eth.jcd.badgers.vfs.sync.server.UserAccount;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DiskWorkerController;
import ch.eth.jcd.badgers.vfs.util.ChannelUtil;

public class AdministrationRemoteInterfaceImpl implements AdministrationRemoteInterface {

	private final ClientLink clientLink;
	private final ServerConfiguration config;
	private static final Logger LOGGER = Logger.getLogger(AdministrationRemoteInterfaceImpl.class);

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
	public DiskRemoteInterface linkNewDisk2(final LinkedDisk linkedDisk, final RemoteInputStream diskFileContent) throws RemoteException, VFSException {
		if (config.diskWithIdExists(linkedDisk.getId())) {
			final String errorMsg = "disk with UUID: " + linkedDisk.getId() + " already exists on Server, cannot be created, delete it first.";
			LOGGER.error(errorMsg);
			throw new VFSException(errorMsg);
		}
		linkedDisk.getDiskConfig().setHostFilePath(createDiskPathFromUUID(linkedDisk.getId()));
		final File tempFile = new File(linkedDisk.getDiskConfig().getHostFilePath());
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(tempFile);
			ChannelUtil.fastStreamCopy(diskFileContent, fos);
		} catch (final IOException e) {
			LOGGER.error("ERROR while uploading disk with UUID: " + linkedDisk.getId() + " to Server", e);
			throw new VFSException("ERROR while uploading disk with UUID: " + linkedDisk.getId() + " to Server", e);
		}
		clientLink.getUserAccount().addLinkedDisk(linkedDisk);
		config.persist();
		return fetchDiskWorkerControllerFromIdAndSetRmiObject(linkedDisk.getId());
	}

	@Override
	public DiskRemoteInterface linkNewDisk(final LinkedDisk linkedDisk, final Journal journal) throws RemoteException, VFSException {
		final UserAccount userAccout = this.clientLink.getUserAccount();

		final File bfsFolder = config.getBfsFileFolder();
		final String bfsFilePath = bfsFolder.getAbsolutePath() + File.separator + linkedDisk.getId() + ".bfs";

		// create server side DiskConfiguration
		final DiskConfiguration diskConfiguration = new DiskConfiguration();
		diskConfiguration.setCompressionAlgorithm(Compression.NONE);
		diskConfiguration.setEncryptionAlgorithm(Encryption.NONE);
		diskConfiguration.setHostFilePath(bfsFilePath);
		diskConfiguration.setMaximumSize(-1);

		// override diskConfiguration
		linkedDisk.setDiskConfiguration(diskConfiguration);
		userAccout.addLinkedDisk(linkedDisk);
		config.persist();

		// Create Disk and DiskManager
		final VFSDiskManagerFactory factory = VFSDiskManagerImplFactory.getInstance();
		final VFSDiskManagerImpl diskManager = (VFSDiskManagerImpl) factory.createDiskManager(diskConfiguration);

		diskManager.replayInitialJournal(journal);

		final DiskWorkerController diskWorkerController = new DiskWorkerController(diskManager);
		final DiskRemoteInterfaceImpl obj = new DiskRemoteInterfaceImpl(diskWorkerController);
		final DiskRemoteInterface stub = (DiskRemoteInterface) UnicastRemoteObject.exportObject(obj, 0);
		return stub;
	}

	@Override
	public DiskRemoteInterface useLinkedDisk(final UUID diskId) throws RemoteException, VFSException {

		return fetchDiskWorkerControllerFromIdAndSetRmiObject(diskId);
	}

	private DiskRemoteInterface fetchDiskWorkerControllerFromIdAndSetRmiObject(final UUID diskId) throws VFSException, RemoteException {
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
		LOGGER.info("closing disk for user Username: " + clientLink.getUserAccount().getUsername());

		UnicastRemoteObject.unexportObject(diskRemoteInterface, true);
	}

	@Override
	public void createNewDisk(final LinkedDisk linkedDiskPrototype) throws RemoteException, VFSException {
		final DiskConfiguration diskConfig = linkedDiskPrototype.getDiskConfig();
		diskConfig.setHostFilePath(createDiskPathFromUUID(linkedDiskPrototype.getId()));
		final VFSDiskManagerFactory factory = VFSDiskManagerFactory.getInstance();
		factory.createDiskManager(diskConfig);
		clientLink.getUserAccount().addLinkedDisk(linkedDiskPrototype);
		config.persist();
	}

	public ClientLink getClientLink() {
		return clientLink;
	}

	@Override
	public void getLinkedDisk(final UUID diskId, final RemoteOutputStream remoteDiskFileContent) throws RemoteException, VFSException {
		final LinkedDisk disk = clientLink.getUserAccount().getLinkedDiskById(diskId);
		if (disk == null) {
			final String errorMsg = "disk with UUID: " + diskId + " does not exists, cannot be fetched";
			LOGGER.error(errorMsg);
			throw new VFSException(errorMsg);
		}
		final File diskToFetch = new File(disk.getDiskConfig().getHostFilePath());
		if (!diskToFetch.exists()) {
			final String errorMsg = "disk with UUID: " + diskId + " does not exists on Server, cannot be fetched";
			LOGGER.error(errorMsg);
			throw new VFSException(errorMsg);
		}
		InputStream is = null;
		try {
			is = new FileInputStream(diskToFetch);
			ChannelUtil.fastStreamCopy(is, remoteDiskFileContent);
		} catch (final IOException e) {
			LOGGER.error("ERROR while fetching disk with UUID: " + diskId + " from Server", e);
			throw new VFSException("ERROR while fetching disk with UUID: " + diskId + " from Server", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (final IOException e) {
					LOGGER.trace(e);
				}
			}
		}
	}

	private String createDiskPathFromUUID(final UUID diskId) {
		return config.getBfsFileFolder().getAbsolutePath() + File.separatorChar + diskId + ".bfs";
	}

	@Override
	public void closeLinkedDisk(final DiskRemoteInterface diskInterface) throws RemoteException, VFSException {
		closeDisk(diskInterface);
	}

}
