package ch.eth.jcd.badgers.vfs.sync.server;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.Initialisation;

public class SynchronisationServer {
	private static final Logger LOGGER = Logger.getLogger(SynchronisationServer.class);

	private final ServerRemoteInterfaceManager ifManager;

	public SynchronisationServer(ServerConfiguration config) {
		ifManager = new ServerRemoteInterfaceManager();
	}

	public void start() throws VFSException {
		LOGGER.info("Start Server");
		ifManager.setup();
	}

	public void stop() throws VFSException {
		LOGGER.info("Stop Server");

	}

	/**
	 * @param args
	 * @throws VFSException
	 */
	public static void main(String[] args) throws VFSException {
		Initialisation.initLog4J(args);

		ServerConfiguration config = Initialisation.parseServerConfiguration(args);
		SynchronisationServer server = new SynchronisationServer(config);

		server.start();
	}
}
