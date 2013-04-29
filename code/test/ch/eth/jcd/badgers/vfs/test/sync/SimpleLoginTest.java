package ch.eth.jcd.badgers.vfs.test.sync;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.sync.client.ConnectionStateListener;
import ch.eth.jcd.badgers.vfs.sync.client.ConnectionStatus;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.sync.server.ServerConfiguration;
import ch.eth.jcd.badgers.vfs.sync.server.SynchronisationServer;
import ch.eth.jcd.badgers.vfs.sync.server.UserAccount;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;

public class SimpleLoginTest implements ConnectionStateListener {

	private static ServerConfiguration serverConfig;
	private static SynchronisationServer syncServer;
	private static RemoteManager clientRemoteManager;
	private static String hostLink = "localhost";

	private static final String username = "asdf";
	private static final String password = "asdf";

	private ConnectionStatus status = ConnectionStatus.DISCONNECTED;

	@BeforeClass
	public static void beforeClass() {
		UnittestLogger.init();
	}

	@Before
	public void before() throws VFSException {
		setupSynchronisationServer();
		setupClient();
	}

	public void after() throws VFSException {
		tearDownClient();
		tearDownSynchronisationServer();
	}

	@Test
	public void testLogin() throws InterruptedException {

		long t = System.currentTimeMillis();
		long timeout = 5000;
		while (status != ConnectionStatus.CONNECTED && (System.currentTimeMillis() - t) < timeout) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
			}
		}

		final CountDownLatch lock = new CountDownLatch(1);
		final ConnectionStatus[] statusResult = new ConnectionStatus[1];
		boolean result = clientRemoteManager.startLogin(username, password, new ConnectionStateListener() {
			@Override
			public void connectionStateChanged(final ConnectionStatus status) {
				statusResult[0] = status;
				lock.countDown();
			}
		});

		Assert.assertTrue(clientRemoteManager.getConnectionStatus() + "", result);
		lock.await(5000, TimeUnit.MILLISECONDS);

		Assert.assertEquals(ConnectionStatus.LOGGED_IN, statusResult[0]);
	}

	private void setupClient() {
		clientRemoteManager = new RemoteManager(hostLink);
		clientRemoteManager.addConnectionStateListener(this);
		clientRemoteManager.start();

	}

	private static void tearDownClient() {
		clientRemoteManager.dispose();
	}

	private static void setupSynchronisationServer() throws VFSException {
		serverConfig = createServerConfiguration();
		syncServer = new SynchronisationServer(serverConfig);
		syncServer.start();
	}

	private static ServerConfiguration createServerConfiguration() throws VFSException {
		final ServerConfiguration config = new ServerConfiguration("");

		if (!config.accountExists(username)) {
			UserAccount userAccount = new UserAccount(username, password);
			config.setUserAccount(userAccount);
		}
		return config;
	}

	private static void tearDownSynchronisationServer() throws VFSException {
		syncServer.stop();
	}

	@Override
	public void connectionStateChanged(ConnectionStatus status) {
		this.status = status;
	}

}
