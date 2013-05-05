package ch.eth.jcd.badgers.vfs.test.sync;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.ClientVersion;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.interfaces.DiskRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.ActiveClientLink;
import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;
import ch.eth.jcd.badgers.vfs.remote.model.PushVersionResult;
import ch.eth.jcd.badgers.vfs.sync.client.ConnectionStateListener;
import ch.eth.jcd.badgers.vfs.sync.client.ConnectionStatus;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.sync.server.ServerConfiguration;
import ch.eth.jcd.badgers.vfs.sync.server.SynchronisationServer;
import ch.eth.jcd.badgers.vfs.sync.server.UserAccount;
import ch.eth.jcd.badgers.vfs.test.testutil.CoreTestUtil;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;

public class SimpleSyncTest implements ConnectionStateListener {
	private static final Logger LOGGER = Logger.getLogger(SimpleSyncTest.class);

	private ConnectionStatus status;

	private SynchronisationServer syncServer;
	private RemoteManager clientRemoteManager;
	private static final String HOST_LINK = "localhost";
	private VFSDiskManagerImpl clientDiskManager;
	private static final String USERNAME = new BigInteger(130, new Random()).toString(32);
	private static final String PASSWORD = "asdf";

	@BeforeClass
	public static void beforeClass() throws VFSException {
		UnittestLogger.init();
	}

	@Before
	public void before() throws VFSException {
		LOGGER.info("Start Synchronisation Server");
		final ServerConfiguration serverConfig = setupServerConfiguration();
		syncServer = new SynchronisationServer(serverConfig);
		syncServer.start();

		LOGGER.info("Start Client");
		clientRemoteManager = new RemoteManager(HOST_LINK);
		clientRemoteManager.addConnectionStateListener(this);
		clientRemoteManager.start();

		LOGGER.info("Create new disk");
		final DiskConfiguration clientConfig = createConfig();
		clientDiskManager = VFSDiskManagerImpl.create(clientConfig);

		fillDiskWithStuff(clientDiskManager);

	}

	private void fillDiskWithStuff(final VFSDiskManagerImpl diskManager) throws VFSException {
		final VFSEntry root = diskManager.getRoot();
		final VFSPath homePath = root.getChildPath("home");
		final VFSPath libPath = root.getChildPath("lib");

		final VFSEntry homeDir = homePath.createDirectory();
		libPath.createDirectory();

		final VFSPath filePath1 = homeDir.getChildPath("anyFile1.bin");
		final VFSEntry fileEntry1 = filePath1.createFile();

		final VFSPath filePath2 = homeDir.getChildPath("anyFile2.bin");
		final VFSEntry fileEntry2 = filePath2.createFile();

		fillInRandomData(fileEntry1);
		fillInRandomData(fileEntry2);
	}

	private void fillInRandomData(final VFSEntry fileEntry) {
		try (OutputStream out = fileEntry.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
			final Random rnd = new Random();
			final byte[] rawData = new byte[rnd.nextInt(1000)];
			rnd.nextBytes(rawData);
			out.write(rawData);
		} catch (IOException | VFSException e) {
			LOGGER.error("", e);
		}
	}

	private DiskConfiguration createConfig() {
		final DiskConfiguration config = new DiskConfiguration();

		final String name = "synctest.bfs";
		final String tempDir = System.getProperty("java.io.tmpdir");
		final String fileName = tempDir + File.separatorChar + name;

		final File f = new File(fileName);
		if (f.exists()) {
			f.delete();
		}

		config.setHostFilePath(fileName);
		return config;
	}

	private ServerConfiguration setupServerConfiguration() throws VFSException {
		final ServerConfiguration config = new ServerConfiguration("");
		final UserAccount userAccount = new UserAccount(USERNAME, PASSWORD);

		if (!config.accountExists(USERNAME)) {
			config.setUserAccount(userAccount);
		}
		return config;
	}

	@After
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

		final boolean result = clientRemoteManager.startLogin(USERNAME, PASSWORD, this);
		Assert.assertTrue(result);

		final long t = System.currentTimeMillis();
		final long timeout = 5000;
		AdministrationRemoteInterface adminRI = null;
		do {
			adminRI = clientRemoteManager.getAdminInterface();
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
			}
		} while (adminRI == null && (System.currentTimeMillis() - t) < timeout);

		Assert.assertNotNull(adminRI);

		LOGGER.info("We are now logged in");

		final String displayName = "TestName";
		final DiskConfiguration diskConfig = new DiskConfiguration();
		final LinkedDisk linkedDisk = new LinkedDisk(displayName, diskConfig);

		final Journal journal = clientDiskManager.linkDisk(HOST_LINK);
		journal.beforeRmiTransport(clientDiskManager);
		final DiskRemoteInterface diskRemoteInterface = adminRI.linkNewDisk(linkedDisk, journal);

		LOGGER.info("Disk is now linked");

		final ClientVersion clientVersion = clientDiskManager.getPendingVersion();
		Assert.assertEquals("expect no local changes", 0, clientVersion.getJournals().size());

		final List<ActiveClientLink> links = syncServer.getActiveClientLinks();
		Assert.assertEquals(1, links.size());
		final ActiveClientLink clientLink = links.get(0);

		final VFSDiskManager syncServerDiskManager = clientLink.getClientLink().getDiskWorkerController().getDiskManager();
		Assert.assertEquals("Expecte Version 1 with initial Journal", 1, syncServerDiskManager.getServerVersion());

		// compare content of the file systems
		CoreTestUtil.assertEntriesEqual(clientDiskManager.getRoot(), syncServerDiskManager.getRoot());

		LOGGER.info("Do local change");
		final VFSPath otherDirectoryPath = clientDiskManager.getRoot().getChildPath("FileWithVersion1Stuff");
		final VFSEntry otherDirectory = otherDirectoryPath.createDirectory();
		final VFSPath otherFilePath = otherDirectory.getChildPath("test.txt");
		final VFSEntry otherFile = otherFilePath.createFile();
		try (OutputStream out = otherFile.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
			out.write("Hello World".getBytes());
		}

		clientDiskManager.closeCurrentJournal();

		LOGGER.info("Do some more local change");
		final VFSPath otherDirectoryPath2 = clientDiskManager.getRoot().getChildPath("FileWithVersion2Stuff");
		otherDirectoryPath2.createDirectory();

		clientDiskManager.closeCurrentJournal();

		final ClientVersion version = clientDiskManager.getPendingVersion();
		final List<Journal> pendingJournals = version.getJournals();
		Assert.assertEquals("expected 2 pending journals", 2, pendingJournals.size());

		version.beforeRmiTransport(clientDiskManager);
		final PushVersionResult pushVersionResult = diskRemoteInterface.pushVersion(version);
		Assert.assertTrue(pushVersionResult.toString(), pushVersionResult.isSuccess());
		Assert.assertEquals(3, pushVersionResult.getNewServerVersion());

		// version is pushed - compare server and client disk
		CoreTestUtil.assertEntriesEqual(clientDiskManager.getRoot(), syncServerDiskManager.getRoot());

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
	public void connectionStateChanged(final ConnectionStatus status) {
		this.status = status;
	}
}
