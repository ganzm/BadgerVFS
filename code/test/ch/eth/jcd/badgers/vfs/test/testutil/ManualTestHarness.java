package ch.eth.jcd.badgers.vfs.test.testutil;

import java.io.File;
import java.util.UUID;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.core.model.Compression;
import ch.eth.jcd.badgers.vfs.core.model.Encryption;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;
import ch.eth.jcd.badgers.vfs.sync.server.ServerConfiguration;
import ch.eth.jcd.badgers.vfs.sync.server.SynchronisationServer;
import ch.eth.jcd.badgers.vfs.sync.server.UserAccount;
import ch.eth.jcd.badgers.vfs.ui.desktop.Initialisation;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.BadgerMainFrame;

public class ManualTestHarness {

	private static UUID diskId = UUID.randomUUID();
	private static String defaultUser = "user";
	private static String defaultPassword = "user";
	private static String syncServerHost = "localhost";
	private static String syncServerConfigPath = "c:\\temp\\badgerserverconfig";

	private static SynchronisationServer server;

	/**
	 * @param args
	 * @throws VFSException
	 */
	public static void main(String[] args) throws VFSException {
		UnittestLogger.init();

		startSynchronizationServer();

		startClientGUI("c:\\temp\\disk1.bfs");
		startClientGUI("c:\\temp\\disk2.bfs");
	}

	private static void deleteFile(String diskFilePath) throws VFSException {
		// delete existing disk file
		File tempFile = new File(diskFilePath);
		if (tempFile.exists()) {
			if (!tempFile.delete()) {
				throw new VFSException("Could not delete File " + diskFilePath);
			}
		}
	}

	private static void startClientGUI(String diskFilePath) throws VFSException {
		deleteFile(diskFilePath);

		// create disk and close it (to be reopened later)
		DiskConfiguration config = new DiskConfiguration();
		config.setDiskId(diskId);
		config.setCompressionAlgorithm(Compression.LZ77);
		config.setEncryptionAlgorithm(Encryption.CAESAR);
		config.setHostFilePath(diskFilePath);
		config.setLinkedHostName(syncServerHost);
		config.setMaximumSize(100000000);

		VFSDiskManagerFactory factory = VFSDiskManagerFactory.getInstance();
		VFSDiskManager diskManager = factory.createDiskManager(config);
		diskManager.close();

		// start GUI
		BadgerMainFrame frame = new BadgerMainFrame();
		frame.update();
		frame.setVisible(true);

		frame.getController().openDiskFromFile(new File(diskFilePath));
	}

	private static void startSynchronizationServer() throws VFSException {
		String[] syncServerArgs = new String[] { "-cc", "-c", syncServerConfigPath };

		String serverDiskFilePath = syncServerConfigPath + File.separator + diskId.toString() + ".bfs";

		deleteFile(serverDiskFilePath);

		ServerConfiguration serverConfig = Initialisation.parseServerConfiguration(syncServerArgs);
		DiskConfiguration serverDiskConfig = new DiskConfiguration();
		serverDiskConfig.setDiskId(diskId);
		serverDiskConfig.setCompressionAlgorithm(Compression.LZ77);
		serverDiskConfig.setEncryptionAlgorithm(Encryption.CAESAR);
		serverDiskConfig.setHostFilePath(serverDiskFilePath);
		serverDiskConfig.setLinkedHostName(syncServerHost);
		serverDiskConfig.setMaximumSize(100000000);

		VFSDiskManagerFactory factory = VFSDiskManagerFactory.getInstance();
		VFSDiskManager diskManager = factory.createDiskManager(serverDiskConfig);
		diskManager.close();

		UserAccount userAccount = new UserAccount(defaultUser, defaultPassword);
		LinkedDisk linkedDisk = new LinkedDisk("Test", serverDiskConfig);
		userAccount.addLinkedDisk(linkedDisk);
		serverConfig.setUserAccount(userAccount);

		server = new SynchronisationServer(serverConfig);
		server.start();
	}

}
