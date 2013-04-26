package ch.eth.jcd.badgers.vfs.test.sync;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.sync.client.ConnectionStateListener;
import ch.eth.jcd.badgers.vfs.sync.client.ConnectionStatus;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.sync.server.ServerConfiguration;
import ch.eth.jcd.badgers.vfs.sync.server.SynchronisationServer;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;

public class SimpleLoginTest implements ConnectionStateListener {

	private static ServerConfiguration serverConfig;
	private static SynchronisationServer syncServer;
	private static RemoteManager clientRemoteManager;
	private static String hostLink = "localhost";

	private ConnectionStatus status;

	private final String username = "user";
	private final String password = "password";

	@BeforeClass
	public static void beforeClass() throws VFSException {
		UnittestLogger.init();

		setupSynchronisationServer();

		setupClient();
	}

	@AfterClass
	public static void afterClass() throws VFSException {
		tearDownClient();
		tearDownSynchronisationServer();
	}

	@Test
	public void testLogin() throws InterruptedException {
		clientRemoteManager.addConnectionStateListener(this);

		final long startTime = System.currentTimeMillis();
		final long timeout = 5000;
		while (status != ConnectionStatus.CONNECTED && System.currentTimeMillis() - startTime < timeout) {
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
			}
		}

		Assert.assertEquals(ConnectionStatus.CONNECTED, status);

		clientRemoteManager.startLogin(username, password);

		Thread.sleep(2000);

	}

	private static void setupClient() {
		clientRemoteManager = new RemoteManager(hostLink);
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
		return config;
	}

	private static void tearDownSynchronisationServer() throws VFSException {
		syncServer.stop();
	}

	@Override
	public void connectionStateChanged(final ConnectionStatus status) {
		this.status = status;
	}
}
