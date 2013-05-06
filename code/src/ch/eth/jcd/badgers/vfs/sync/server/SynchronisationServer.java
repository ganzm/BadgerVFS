package ch.eth.jcd.badgers.vfs.sync.server;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.ifimpl.DiskRemoteInterfaceImpl;
import ch.eth.jcd.badgers.vfs.remote.model.ActiveClientLink;
import ch.eth.jcd.badgers.vfs.ui.desktop.Initialisation;

public class SynchronisationServer {
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

		final ServerConfiguration config = Initialisation.parseServerConfiguration(args);
		final SynchronisationServer server = new SynchronisationServer(config);

		server.start();
	}
}
