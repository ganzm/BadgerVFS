package ch.eth.jcd.badgers.vfs.test.sync;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.interfaces.DiskRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;
import ch.eth.jcd.badgers.vfs.sync.client.ConnectionStatus;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.sync.server.ServerConfiguration;
import ch.eth.jcd.badgers.vfs.sync.server.SynchronisationServer;
import ch.eth.jcd.badgers.vfs.sync.server.UserAccount;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;

public class TwoWaySyncTest {
	private static final Logger LOGGER = Logger.getLogger(TwoWaySyncTest.class);

	private ServerConfiguration serverConfig;
	private SynchronisationServer syncServer;
	private RemoteManager clientRemoteManager1;
	private RemoteManager clientRemoteManager2;
	private final String hostLink = "localhost";
	private VFSDiskManagerImpl clientDiskManager1;
	private VFSDiskManagerImpl clientDiskManager2;
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

		LOGGER.info("Start Client1");
		clientRemoteManager1 = new RemoteManager(hostLink);
		clientRemoteManager1.start();

		LOGGER.info("Start Client2");
		clientRemoteManager2 = new RemoteManager(hostLink);
		clientRemoteManager2.start();
		//
		// LOGGER.info("Create new disk");
		// DiskConfiguration clientConfig = createConfig();
		// clientDiskManager = VFSDiskManagerImpl.create(clientConfig);
		//
		// fillDiskWithStuff(clientDiskManager);

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

	private DiskConfiguration createConfig(String hostLink, String diskName, UUID diskUuid, String fileSuffix) {
		DiskConfiguration config = new DiskConfiguration();

		String name = "synctest" + fileSuffix;
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
		LOGGER.info("Shutdown client1");
		clientRemoteManager1.dispose();

		LOGGER.info("Shutdown client2");
		clientRemoteManager2.dispose();

		LOGGER.info("Shutdown SynchronisationServer");
		syncServer.stop();
	}

	@Test
	public void testSimpleSync() throws VFSException, IOException {
		LOGGER.info("Start login");
		waitUntilConnected();

		boolean result1 = clientRemoteManager1.startLogin(username, password, null);
		boolean result2 = clientRemoteManager2.startLogin(username, password, null);
		Assert.assertTrue(result1);
		Assert.assertTrue(result2);

		AdministrationRemoteInterface clientAdminInterface1 = clientRemoteManager1.getAdminInterface();
		AdministrationRemoteInterface clientAdminInterface2 = clientRemoteManager2.getAdminInterface();

		LOGGER.info("Client1 creates a new RemoteDisk");
		String diskName = "UnitTest-" + System.currentTimeMillis();
		List<LinkedDisk> availableRemoteDisksBefore = clientAdminInterface1.listDisks();
		UUID diskUuid = clientAdminInterface1.createNewDisk(diskName);
		List<LinkedDisk> availableRemoteDisksAfter = clientAdminInterface1.listDisks();
		Assert.assertTrue(availableRemoteDisksBefore.size() + 1 == availableRemoteDisksAfter.size());

		DiskRemoteInterface diskRemoteInterface1 = clientAdminInterface1.useLinkedDisk(diskUuid);
		DiskRemoteInterface diskRemoteInterface2 = clientAdminInterface2.useLinkedDisk(diskUuid);

		VFSDiskManagerFactory factory = VFSDiskManagerFactory.getInstance();

		DiskConfiguration clientDiskConfig1 = createConfig(hostLink, diskName, diskUuid, ".client1.bfs");
		clientDiskManager1 = (VFSDiskManagerImpl) factory.createDiskManager(clientDiskConfig1);

		DiskConfiguration clientDiskConfig2 = createConfig(hostLink, diskName, diskUuid, ".client2.bfs");
		clientDiskManager2 = (VFSDiskManagerImpl) factory.createDiskManager(clientDiskConfig2);

		long lastSeenServerVersion1 = clientDiskManager1.getServerVersion();
		List<Journal> versionDelta = diskRemoteInterface1.getVersionDelta(lastSeenServerVersion1, -1);

		// do stuff on the server

		// TODO

		// connect client 2

		// TODO

		//
		// clientAdminInterface1.getLinkedDisk(diskId, remoteDiskFileContent)
		//
		// long t = System.currentTimeMillis();
		// long timeout = 5000;
		// AdministrationRemoteInterface adminRI = null;
		// do {
		// adminRI = clientRemoteManager.getAdminInterface();
		// try {
		// Thread.sleep(100);
		// } catch (InterruptedException e) {
		// }
		// } while (adminRI == null && (System.currentTimeMillis() - t) < timeout);
		//
		// Assert.assertNotNull(adminRI);
		//
		// LOGGER.info("We are now logged in");
		//
		// String displayName = "TestName";
		// DiskConfiguration diskConfig = new DiskConfiguration();
		// LinkedDisk linkedDisk = new LinkedDisk(displayName, diskConfig);
		//
		// Journal journal = clientDiskManager.linkDisk(hostLink);
		// journal.beforeRmiTransport(clientDiskManager);
		// DiskRemoteInterface diskRemoteInterface = adminRI.linkNewDisk(linkedDisk, journal);
		//
		// LOGGER.info("Disk is now linked");
		//
		// ClientVersion clientVersion = clientDiskManager.getPendingVersion();
		// Assert.assertEquals("expect no local changes", 0, clientVersion.getJournals().size());
		//
		// List<ClientLink> links = syncServer.getActiveClientLinks();
		// Assert.assertEquals(1, links.size());
		// ClientLink clientLink = links.get(0);
		//
		// VFSDiskManager syncServerDiskManager = clientLink.getDiskWorkerController().getDiskManager();
		// Assert.assertEquals("Expecte initial Version 0", 0, syncServerDiskManager.getServerVersion());
		//
		// // compare content of the file systems
		// CoreTestUtil.assertEntriesEqual(clientDiskManager.getRoot(), syncServerDiskManager.getRoot());
		//
		// LOGGER.info("Do local change");
		// VFSPath otherDirectoryPath = clientDiskManager.getRoot().getChildPath("FileWithVersion1Stuff");
		// VFSEntry otherDirectory = otherDirectoryPath.createDirectory();
		// VFSPath otherFilePath = otherDirectory.getChildPath("test.txt");
		// VFSEntry otherFile = otherFilePath.createFile();
		// try (OutputStream out = otherFile.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
		// out.write("Hello World".getBytes());
		// }
		//
		// clientDiskManager.closeCurrentJournal();
		//
		// LOGGER.info("Do some more local change");
		// VFSPath otherDirectoryPath2 = clientDiskManager.getRoot().getChildPath("FileWithVersion2Stuff");
		// otherDirectoryPath2.createDirectory();
		//
		// clientDiskManager.closeCurrentJournal();
		//
		// ClientVersion version = clientDiskManager.getPendingVersion();
		// List<Journal> pendingJournals = version.getJournals();
		// Assert.assertEquals("expected 2 pending journals", 2, pendingJournals.size());
		//
		// version.beforeRmiTransport(clientDiskManager);
		// PushVersionResult pushVersionResult = diskRemoteInterface.pushVersion(version);
		// Assert.assertTrue(pushVersionResult.toString(), pushVersionResult.isSuccess());
		// Assert.assertEquals(2, pushVersionResult.getNewServerVersion());
		//
		// // version is pushed - compare server and client disk
		// CoreTestUtil.assertEntriesEqual(clientDiskManager.getRoot(), syncServerDiskManager.getRoot());
		//
		LOGGER.info("Unlink Disk");
		diskRemoteInterface1.unlink();
		diskRemoteInterface2.unlink();
	}

	private void waitUntilConnected() {
		final long startTime = System.currentTimeMillis();
		final long timeout = 5000;

		// yes we are polling here - but hey who cares it's just a UnitTest
		while (System.currentTimeMillis() - startTime < timeout) {
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
			}
			if (clientRemoteManager1.getConnectionStatus() == ConnectionStatus.CONNECTED
					&& clientRemoteManager2.getConnectionStatus() == ConnectionStatus.CONNECTED) {
				break;
			}
		}

		if (clientRemoteManager1.getConnectionStatus() != ConnectionStatus.CONNECTED
				|| clientRemoteManager2.getConnectionStatus() != ConnectionStatus.CONNECTED) {
			Assert.fail("Expected both clients to be connected");
		}
	}
}
