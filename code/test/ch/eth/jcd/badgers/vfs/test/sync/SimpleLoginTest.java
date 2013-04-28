package ch.eth.jcd.badgers.vfs.test.sync;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

	private final String username = "asdf";
	private final String password = "asdf";

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
		final CountDownLatch lock = new CountDownLatch(1);

		final ConnectionStatus[] statusResult = new ConnectionStatus[1];
		clientRemoteManager.startLogin(username, password, new ConnectionStateListener() {

			@Override
			public void connectionStateChanged(final ConnectionStatus status) {

				statusResult[0] = status;
				lock.countDown();

			}
		});
		lock.await(5000, TimeUnit.MILLISECONDS);

		Assert.assertEquals(ConnectionStatus.LOGGED_IN, statusResult[0]);

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
