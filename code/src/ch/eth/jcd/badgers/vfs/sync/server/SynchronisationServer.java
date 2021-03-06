package ch.eth.jcd.badgers.vfs.sync.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.ifimpl.DiskRemoteInterfaceImpl;
import ch.eth.jcd.badgers.vfs.remote.model.ActiveClientLink;
import ch.eth.jcd.badgers.vfs.ui.desktop.Initialisation;

public class SynchronisationServer {
	private static final String JAVA_RMI_SERVER_HOSTNAME = "java.rmi.server.hostname";

	private static final Logger LOGGER = Logger.getLogger(SynchronisationServer.class);

	private final ServerRemoteInterfaceManager ifManager;

	public SynchronisationServer(final ServerConfiguration config) {
		ifManager = new ServerRemoteInterfaceManager(config);
	}

	public void start() throws VFSException {
		LOGGER.info("Start Server");
		ifManager.setup();
	}

	public void stop() throws VFSException {
		LOGGER.info("Stop Server");
		ifManager.dispose();
	}

	public List<ActiveClientLink> getActiveClientLinks() {
		return ifManager.getActiveClientLinks();
	}

	public Map<UUID, DiskRemoteInterfaceImpl> activeDiskRemoteInterfaceImpls() {
		return ifManager.getActiveDiskRemoteInterfaceImpls();
	}

	/**
	 * @param args
	 * @throws VFSException
	 */
	public static void main(final String[] args) throws VFSException {
		Initialisation.initLog4J(args);

		logMyIps();

		final ServerConfiguration config = Initialisation.parseServerConfiguration(args);

		handleRmiServerHostName(args);

		final SynchronisationServer server = new SynchronisationServer(config);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOGGER.debug("inside shutdown hook");
				try {
					server.stop();
				} catch (VFSException e) {
					LOGGER.error("error shutting down", e);
				}
			}
		});
		server.start();

		BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
		String input;
		try {
			while ((input = inputReader.readLine()) != null) {
				if ("quit".equals(input)) {
					server.stop();
					System.exit(0);
				}
			}
		} catch (IOException e) {
			LOGGER.equals("error reading input");
		}

	}

	private static void handleRmiServerHostName(final String[] args) {
		String rmiHost = Initialisation.getCommandLineArgumentValue(args, "-host");
		if (rmiHost == null || rmiHost.isEmpty()) {
			LOGGER.warn("Missing start parameter -host <RMI Host>");
			rmiHost = System.getProperty(JAVA_RMI_SERVER_HOSTNAME, rmiHost);
			if (rmiHost == null) {
				rmiHost = getDefaultHostName();
				System.setProperty(JAVA_RMI_SERVER_HOSTNAME, rmiHost);
			}
			LOGGER.warn("Assume as host name " + rmiHost);
		} else {
			System.setProperty(JAVA_RMI_SERVER_HOSTNAME, rmiHost);
		}
	}

	private static String getDefaultHostName() {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				if (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					return addr.getHostAddress();
				}
			}
		} catch (SocketException e) {
			LOGGER.error("", e);
		}

		return null;
	}

	private static void logMyIps() {
		LOGGER.info("Logging my Network Interfaces");
		String ip;
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				// filters out 127.0.0.1 and inactive interfaces
				if (iface.isLoopback() || !iface.isUp()) {
					continue;
				}

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					ip = addr.getHostAddress();
					LOGGER.info(iface.getDisplayName() + "\t" + ip);
				}
			}
		} catch (SocketException e) {
			LOGGER.error("error in logMyIps", e);
		}
		LOGGER.info("Logging my Network Interfaces done");
	}
}
