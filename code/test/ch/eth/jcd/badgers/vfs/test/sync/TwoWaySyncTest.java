package ch.eth.jcd.badgers.vfs.test.sync;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.ClientVersion;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.core.model.Compression;
import ch.eth.jcd.badgers.vfs.core.model.Encryption;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.interfaces.DiskRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.ActiveClientLink;
import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;
import ch.eth.jcd.badgers.vfs.remote.model.PushVersionResult;
import ch.eth.jcd.badgers.vfs.sync.client.ConnectionStatus;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.sync.server.ServerConfiguration;
import ch.eth.jcd.badgers.vfs.sync.server.SynchronisationServer;
import ch.eth.jcd.badgers.vfs.sync.server.UserAccount;
import ch.eth.jcd.badgers.vfs.test.testutil.CoreTestUtil;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;
import ch.eth.jcd.badgers.vfs.ui.desktop.Initialisation;

public class TwoWaySyncTest {
	private static final Logger LOGGER = Logger.getLogger(TwoWaySyncTest.class);

	private SynchronisationServer syncServer;
	private RemoteManager clientRemoteManager1;
	private RemoteManager clientRemoteManager2;
	private static final String HOST_LINK = "localhost";
	private VFSDiskManager serverDiskManager;

	private static final String USERNAME = new BigInteger(130, new Random()).toString(32);
	private static final String PASSWORD = "asdf";

	@BeforeClass
	public static void beforeClass() throws VFSException {
		UnittestLogger.init();
	}

	@Before
	public void before() throws VFSException {
		LOGGER.info("Start Synchronisation Server");
		ServerConfiguration serverConfig = setupServerConfiguration();
		syncServer = new SynchronisationServer(serverConfig);
		syncServer.start();

		LOGGER.info("Start Client1");
		clientRemoteManager1 = new RemoteManager(HOST_LINK);
		clientRemoteManager1.start();

		LOGGER.info("Start Client2");
		clientRemoteManager2 = new RemoteManager(HOST_LINK);
		clientRemoteManager2.start();
	}

	@After
	public void after() throws VFSException {
		LOGGER.info("Shutdown client1");
		clientRemoteManager1.dispose();

		LOGGER.info("Shutdown client2");
		clientRemoteManager2.dispose();

		LOGGER.info("Shutdown SynchronisationServer");
		syncServer.stop();
	}

	@Test
	public void testNonConflictingSync() throws VFSException, IOException {
		LOGGER.info("Start login");
		waitForConnectionStatus(ConnectionStatus.CONNECTED);

		boolean result1 = clientRemoteManager1.startLogin(USERNAME, PASSWORD, null);
		boolean result2 = clientRemoteManager2.startLogin(USERNAME, PASSWORD, null);
		Assert.assertTrue(result1);
		Assert.assertTrue(result2);

		waitForConnectionStatus(ConnectionStatus.LOGGED_IN);

		AdministrationRemoteInterface clientAdminInterface1 = clientRemoteManager1.getAdminInterface();
		AdministrationRemoteInterface clientAdminInterface2 = clientRemoteManager2.getAdminInterface();

		LOGGER.info("Client1 creates a new RemoteDisk");
		String diskName = "UnitTest-" + System.currentTimeMillis();
		List<LinkedDisk> availableRemoteDisksBefore = clientAdminInterface1.listDisks();
		UUID diskUuid = clientAdminInterface1.createNewDisk(diskName);
		List<LinkedDisk> availableRemoteDisksAfter = clientAdminInterface1.listDisks();
		Assert.assertTrue(availableRemoteDisksBefore.size() + 1 == availableRemoteDisksAfter.size());

		LOGGER.info("Both clients use the new disk");
		DiskRemoteInterface diskRemoteInterface1 = clientAdminInterface1.useLinkedDisk(diskUuid);
		DiskRemoteInterface diskRemoteInterface2 = clientAdminInterface2.useLinkedDisk(diskUuid);

		setServerDiskManager();

		// check Version on Server
		Assert.assertEquals("Expect Version 0 on Server", 0, serverDiskManager.getServerVersion());

		VFSDiskManagerFactory factory = VFSDiskManagerFactory.getInstance();

		DiskConfiguration clientDiskConfig1 = createConfig(HOST_LINK, ".client1.bfs");
		VFSDiskManagerImpl clientDiskManager1 = (VFSDiskManagerImpl) factory.createDiskManager(clientDiskConfig1);

		DiskConfiguration clientDiskConfig2 = createConfig(HOST_LINK, ".client2.bfs");
		VFSDiskManagerImpl clientDiskManager2 = (VFSDiskManagerImpl) factory.createDiskManager(clientDiskConfig2);

		// Both clients now have their local disk created and are in sync

		long lastSeenServerVersion1 = clientDiskManager1.getServerVersion();
		List<Journal> versionDelta1 = diskRemoteInterface1.getVersionDelta(lastSeenServerVersion1);
		Assert.assertEquals("Expected no new version", 0, versionDelta1.size());
		diskRemoteInterface1.downloadFinished();

		long lastSeenServerVersion2 = clientDiskManager2.getServerVersion();
		List<Journal> versionDelta2 = diskRemoteInterface2.getVersionDelta(lastSeenServerVersion2);
		Assert.assertEquals("Expected no new version", 0, versionDelta2.size());
		diskRemoteInterface2.downloadFinished();

		LOGGER.info("Everything is synched now");

		LOGGER.info("Client1 does some nonconflicting changes");
		doNonConflictingChanges(clientDiskManager1, ".1");

		LOGGER.info("Client2 does some nonconflicting changes");
		doNonConflictingChanges(clientDiskManager2, ".2");

		LOGGER.info("Client1 pushes changes to Server");
		ClientVersion clientVersion1 = clientDiskManager1.getPendingVersion();
		clientVersion1.beforeRmiTransport(clientDiskManager1);
		PushVersionResult pushResult1 = diskRemoteInterface1.pushVersion(clientVersion1);
		Assert.assertTrue("Expect Version update to be successfull", pushResult1.isSuccess());
		Assert.assertEquals("Expected Server to be Version 1", 1, pushResult1.getNewServerVersion());
		clientDiskManager1.setSynchronized(pushResult1.getNewServerVersion());

		// check Version on Server
		Assert.assertEquals("Expect Version 1 on Server", 1, serverDiskManager.getServerVersion());

		LOGGER.info("Client2 pushes changes to Server");
		ClientVersion clientVersion2 = clientDiskManager2.getPendingVersion();
		clientVersion2.beforeRmiTransport(clientDiskManager2);
		PushVersionResult pushResult2 = diskRemoteInterface2.pushVersion(clientVersion2);
		Assert.assertFalse("Expect Version update to be unsuccessfull because client already updated Server to Version 1", pushResult2.isSuccess());

		LOGGER.info("Client2 needs to update to version 1 before he can push his changes");
		performUpdate(clientDiskManager2, diskRemoteInterface2, 0);

		lastSeenServerVersion2 = clientDiskManager1.getServerVersion();
		Assert.assertEquals(1, lastSeenServerVersion2);

		LOGGER.info("Client2 pushes changes to Server again");
		clientVersion2 = clientDiskManager2.getPendingVersion();
		clientVersion2.beforeRmiTransport(clientDiskManager2);
		pushResult2 = diskRemoteInterface2.pushVersion(clientVersion2);
		Assert.assertTrue("Expect Version update to be successfull but - " + pushResult2.getMessage(), pushResult2.isSuccess());
		Assert.assertEquals("Expected Server to be Version 2", 2, pushResult2.getNewServerVersion());
		clientDiskManager2.setSynchronized(pushResult2.getNewServerVersion());

		LOGGER.info("Client1 updates from Version 1 to Version 2");
		performUpdate(clientDiskManager1, diskRemoteInterface1, 1);

		LOGGER.info("Expect both clients to have the same content on their disks");
		CoreTestUtil.assertEntriesEqual(clientDiskManager1.getRoot(), clientDiskManager2.getRoot());

		LOGGER.info("Unlink Disk");
		diskRemoteInterface1.unlink();
		diskRemoteInterface2.unlink();
	}

	private void setServerDiskManager() {
		List<ActiveClientLink> activeLinks = syncServer.getActiveClientLinks();
		Assert.assertEquals(2, activeLinks.size());

		ActiveClientLink link1 = activeLinks.get(0);
		ActiveClientLink link2 = activeLinks.get(1);

		Assert.assertSame(link1.getClientLink().getDiskWorkerController(), link2.getClientLink().getDiskWorkerController());

		serverDiskManager = link1.getClientLink().getDiskWorkerController().getDiskManager();
	}

	private void performUpdate(VFSDiskManagerImpl clientDiskManager, DiskRemoteInterface diskRemoteInterface, long expectedServerVersionOnClient)
			throws RemoteException, VFSException {
		long lastSeenServerVersion = clientDiskManager.getServerVersion();
		Assert.assertEquals(expectedServerVersionOnClient, lastSeenServerVersion);
		List<Journal> toUpdate = diskRemoteInterface.getVersionDelta(lastSeenServerVersion);
		for (Journal j : toUpdate) {
			j.replay(clientDiskManager);

			clientDiskManager.setServerVersion(clientDiskManager.getServerVersion() + 1);
		}

		diskRemoteInterface.downloadFinished();
	}

	/**
	 * create client side configuration
	 * 
	 */
	private DiskConfiguration createConfig(String hostLink, String fileSuffix) {
		DiskConfiguration config = new DiskConfiguration();

		String name = "synctest" + fileSuffix;
		String tempDir = System.getProperty("java.io.tmpdir");
		String fileName = tempDir + File.separatorChar + name;

		File f = new File(fileName);
		if (f.exists()) {
			Assert.assertTrue(f.delete());
		}

		config.setLinkedHostName(hostLink);
		config.setHostFilePath(fileName);
		config.setCompressionAlgorithm(Compression.NONE);
		config.setEncryptionAlgorithm(Encryption.NONE);
		return config;
	}

	private ServerConfiguration setupServerConfiguration() throws VFSException {
		ServerConfiguration config = Initialisation.parseServerConfiguration(new String[] { "-cc" });
		UserAccount userAccount = new UserAccount(USERNAME, PASSWORD);

		if (!config.accountExists(USERNAME)) {
			config.setUserAccount(userAccount);
		}
		return config;
	}

	private void doNonConflictingChanges(VFSDiskManager clientDiskManager, String suffix) throws VFSException, IOException {
		VFSEntry root = clientDiskManager.getRoot();
		VFSPath dirCPath = root.getChildPath("dirClient" + suffix);
		VFSEntry dirC = dirCPath.createDirectory();
		VFSPath testFilePath = dirC.getChildPath("test.txt");
		VFSEntry testFile = testFilePath.createFile();
		try (OutputStream out = testFile.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
			out.write("Hallo Welt".getBytes());
		}
	}

	private void waitForConnectionStatus(ConnectionStatus expectedStatus) {
		final long startTime = System.currentTimeMillis();
		final long timeout = 5000;

		// yes we are polling here - but hey who cares it's just a UnitTest
		while (System.currentTimeMillis() - startTime < timeout) {
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
			}
			if (clientRemoteManager1.getConnectionStatus() == expectedStatus && clientRemoteManager2.getConnectionStatus() == expectedStatus) {
				break;
			}
		}

		if (clientRemoteManager1.getConnectionStatus() != expectedStatus || clientRemoteManager2.getConnectionStatus() != expectedStatus) {
			Assert.fail("Expected both clients to be " + expectedStatus);
		}
	}
}
