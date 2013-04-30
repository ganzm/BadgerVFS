package ch.eth.jcd.badgers.vfs.test.sync;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.interfaces.DiskRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;
import ch.eth.jcd.badgers.vfs.sync.client.ConnectionStateListener;
import ch.eth.jcd.badgers.vfs.sync.client.ConnectionStatus;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.sync.server.ClientLink;
import ch.eth.jcd.badgers.vfs.sync.server.ServerConfiguration;
import ch.eth.jcd.badgers.vfs.sync.server.SynchronisationServer;
import ch.eth.jcd.badgers.vfs.sync.server.UserAccount;
import ch.eth.jcd.badgers.vfs.test.testutil.CoreTestUtil;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;

public class SimpleSyncTest implements ConnectionStateListener {
	private static final Logger LOGGER = Logger.getLogger(SimpleSyncTest.class);

	private ConnectionStatus status;

	private ServerConfiguration serverConfig;
	private SynchronisationServer syncServer;
	private RemoteManager clientRemoteManager;
	private final String hostLink = "localhost";
	private VFSDiskManagerImpl clientDiskManager;
	private final String username = new BigInteger(130, new Random()).toString(32);
	private final String password = "asdf";

	@BeforeClass
	public static void beforeClass() throws VFSException {
		UnittestLogger.init();
	}

	@Before
	public void before() throws VFSException {
		LOGGER.info("Start Synchronisation Server");
		serverConfig = setupServerConfiguration();
		syncServer = new SynchronisationServer(serverConfig);
		syncServer.start();

		LOGGER.info("Start Client");
		clientRemoteManager = new RemoteManager(hostLink);
		clientRemoteManager.addConnectionStateListener(this);
		clientRemoteManager.start();

		LOGGER.info("Create new disk");
		DiskConfiguration clientConfig = createConfig();
		clientDiskManager = VFSDiskManagerImpl.create(clientConfig);

		fillDiskWithStuff(clientDiskManager);

	}

	private void fillDiskWithStuff(VFSDiskManagerImpl diskManager) throws VFSException {
		VFSEntry root = diskManager.getRoot();
		VFSPath homePath = root.getChildPath("home");
		VFSPath libPath = root.getChildPath("lib");

		VFSEntry homeDir = homePath.createDirectory();
		libPath.createDirectory();

		VFSPath filePath1 = homeDir.getChildPath("anyFile1.bin");
		VFSEntry fileEntry1 = filePath1.createFile();

		VFSPath filePath2 = homeDir.getChildPath("anyFile2.bin");
		VFSEntry fileEntry2 = filePath2.createFile();

		fillInRandomData(fileEntry1);
		fillInRandomData(fileEntry2);
	}

	private void fillInRandomData(VFSEntry fileEntry) {
		try (OutputStream out = fileEntry.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
			Random rnd = new Random();
			byte[] rawData = new byte[rnd.nextInt(1000)];
			rnd.nextBytes(rawData);
			out.write(rawData);
		} catch (IOException | VFSException e) {
			LOGGER.error("", e);
		}
	}

	private DiskConfiguration createConfig() {
		DiskConfiguration config = new DiskConfiguration();

		String name = "synctest.bfs";
		String tempDir = System.getProperty("java.io.tmpdir");
		String fileName = tempDir + File.separatorChar + name;

		File f = new File(fileName);
		if (f.exists()) {
			f.delete();
		}

		config.setHostFilePath(fileName);
		return config;
	}

	private ServerConfiguration setupServerConfiguration() throws VFSException {
		ServerConfiguration config = new ServerConfiguration("");
		UserAccount userAccount = new UserAccount(username, password);

		if (!config.accountExists(username)) {
			config.setUserAccount(userAccount);
		}
		return config;
	}

	public void after() throws VFSException {
		LOGGER.info("Shutdown client");
		clientRemoteManager.dispose();

		LOGGER.info("Shutdown SynchronisationServer");
		syncServer.stop();
	}

	@Test
	public void testSimpleSync() throws VFSException, IOException {
		LOGGER.info("Start login");

		waitUntilConnected();

		boolean result = clientRemoteManager.startLogin(username, password, this);
		Assert.assertTrue(result);

		long t = System.currentTimeMillis();
		long timeout = 5000;
		AdministrationRemoteInterface adminRI = null;
		do {
			adminRI = clientRemoteManager.getAdminInterface();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		} while (adminRI == null && (System.currentTimeMillis() - t) < timeout);

		Assert.assertNotNull(adminRI);

		LOGGER.info("We are now logged in");

		String displayName = "TestName";
		DiskConfiguration diskConfig = new DiskConfiguration();
		LinkedDisk linkedDisk = new LinkedDisk(displayName, diskConfig);

		Journal journal = clientDiskManager.linkDisk(hostLink);
		journal.beforeRmiTransport();
		DiskRemoteInterface diskRemoteInterface = adminRI.linkNewDisk(linkedDisk, journal);

		LOGGER.info("Disk is now linked");

		List<ClientLink> links = syncServer.getActiveClientLinks();
		Assert.assertEquals(1, links.size());
		ClientLink clientLink = links.get(0);

		VFSDiskManager syncServerDiskManager = clientLink.getDiskWorkerController().getDiskManager();

		// compare content of the file systems
		CoreTestUtil.assertEntriesEqual(clientDiskManager.getRoot(), syncServerDiskManager.getRoot());

		LOGGER.info("Do local change");
		VFSPath otherDirectoryPath = clientDiskManager.getRoot().getChildPath("FileWithVersion1Stuff");
		VFSEntry otherDirectory = otherDirectoryPath.createDirectory();
		VFSPath otherFilePath = otherDirectory.getChildPath("test.txt");
		VFSEntry otherFile = otherFilePath.createFile();
		try (OutputStream out = otherFile.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
			out.write("Hello World".getBytes());
		}

		clientDiskManager.closeCurrentJournal();

		LOGGER.info("Do some more local change");
		VFSPath otherDirectoryPath2 = clientDiskManager.getRoot().getChildPath("FileWithVersion2Stuff");
		otherDirectoryPath2.createDirectory();

		clientDiskManager.closeCurrentJournal();

		List<Journal> pendingJournals = clientDiskManager.getPendingJournals();
		Assert.assertEquals("expected 2 pending journals", 2, pendingJournals.size());

		for (Journal toUpload : pendingJournals) {
			diskRemoteInterface.pushVersion(toUpload.getServerVersion(), toUpload);
		}

		LOGGER.info("Unlink Disk");
		diskRemoteInterface.unlink();
	}

	private void waitUntilConnected() {
		final long startTime = System.currentTimeMillis();
		final long timeout = 5000;
		while (status != ConnectionStatus.CONNECTED && System.currentTimeMillis() - startTime < timeout) {
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
			}
		}
	}

	@Override
	public void connectionStateChanged(ConnectionStatus status) {
		this.status = status;
	}
}
