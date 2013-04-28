package ch.eth.jcd.badgers.vfs.test.sync;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
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
import ch.eth.jcd.badgers.vfs.sync.server.ServerConfiguration;
import ch.eth.jcd.badgers.vfs.sync.server.SynchronisationServer;
import ch.eth.jcd.badgers.vfs.sync.server.UserAccount;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class SimpleSyncTest implements ActionObserver, ConnectionStateListener {

	private static final Logger LOGGER = Logger.getLogger(SimpleSyncTest.class);

	private ConnectionStatus status;

	private ServerConfiguration serverConfig;
	private SynchronisationServer syncServer;
	private RemoteManager clientRemoteManager;
	private String hostLink = "localhost";
	private VFSDiskManagerImpl diskManager;
	private final String username = "user";
	private final String password = "password";

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
		diskManager = VFSDiskManagerImpl.create(clientConfig);

		fillSomeJunk(diskManager);

	}

	private void fillSomeJunk(VFSDiskManagerImpl diskManager) throws VFSException {
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
	public void testSimpleSync() throws RemoteException, VFSException {
		LOGGER.info("Start login");

		WaitUntilConnected();

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

		LOGGER.info("We are now loged in");

		String displayName = "TestName";
		DiskConfiguration diskConfig = new DiskConfiguration();
		LinkedDisk linkedDisk = new LinkedDisk(displayName, diskConfig);

		Journal journal = diskManager.linkDisk(hostLink);
		DiskRemoteInterface diskRemoteInterface = adminRI.linkNewDisk(linkedDisk, journal);
	}

	private void WaitUntilConnected() {
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
	public void onActionFailed(AbstractBadgerAction action, Exception e) {
	}

	@Override
	public void onActionFinished(AbstractBadgerAction action) {

	}

	@Override
	public void connectionStateChanged(ConnectionStatus status) {
		this.status = status;
	}
}
