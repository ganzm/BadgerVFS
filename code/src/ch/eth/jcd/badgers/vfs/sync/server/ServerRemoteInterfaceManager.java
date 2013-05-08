package ch.eth.jcd.badgers.vfs.sync.server;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.ifimpl.DiskRemoteInterfaceImpl;
import ch.eth.jcd.badgers.vfs.remote.ifimpl.LoginRemoteInterfaceImpl;
import ch.eth.jcd.badgers.vfs.remote.interfaces.LoginRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.ActiveClientLink;

public class ServerRemoteInterfaceManager {
	private static final Logger LOGGER = Logger.getLogger(ServerRemoteInterfaceManager.class);
	private final ServerConfiguration config;

	private final List<ActiveClientLink> activeClientLinks;
	private final Map<UUID, DiskRemoteInterfaceImpl> activeDiskRemoteInterfaceImpls = new HashMap<>();
	/**
	 * instance to the single LoginInterface published by RMI
	 */
	private LoginRemoteInterface loginRemoteInterface;

	private LoginRemoteInterfaceImpl loginRemoteInterfaceImpl;

	/**
	 * Reference to the RMI Registry
	 */
	private Registry rmiRegistry;

	public ServerRemoteInterfaceManager(final ServerConfiguration config) {
		this.config = config;
		this.activeClientLinks = Collections.synchronizedList(new ArrayList<ActiveClientLink>());
	}

	public void setup() throws VFSException {
		try {
			int port = Registry.REGISTRY_PORT;
			LOGGER.info("Create RMI Registry on Port " + port);
			rmiRegistry = LocateRegistry.createRegistry(port);

			loginRemoteInterfaceImpl = new LoginRemoteInterfaceImpl(this);
			loginRemoteInterface = (LoginRemoteInterface) UnicastRemoteObject.exportObject(loginRemoteInterfaceImpl, 0);

			// Bind the remote object's stub in the registry.
			rmiRegistry.bind(LoginRemoteInterface.LOGIN_INTERFACE_KEY, loginRemoteInterface);

			LOGGER.info("Server ready on port " + Registry.REGISTRY_PORT);
		} catch (final AlreadyBoundException | RemoteException e) {
			throw new VFSException(e);
		}
	}

	public ServerConfiguration getConfig() {
		return config;
	}

	public void dispose() {
		try {
			rmiRegistry.unbind(LoginRemoteInterface.LOGIN_INTERFACE_KEY);

			if (!UnicastRemoteObject.unexportObject(loginRemoteInterfaceImpl, true)) {
				LOGGER.error("RMI Unexport failed for " + loginRemoteInterfaceImpl);
			}

			if (!UnicastRemoteObject.unexportObject(rmiRegistry, true)) {
				LOGGER.error("RMI Registry unexport failed");
			}

		} catch (RemoteException | NotBoundException e) {
			LOGGER.error("Error on server dispose", e);
		}
	}

	public List<ActiveClientLink> getActiveClientLinks() {
		return activeClientLinks;
	}

	public void addActiveClientLink(ActiveClientLink clientLink) {
		activeClientLinks.add(clientLink);
	}

	public Map<UUID, DiskRemoteInterfaceImpl> getActiveDiskRemoteInterfaceImpls() {
		return activeDiskRemoteInterfaceImpls;
	}

	public void addActiveDiskRemoteInterfaceImpls(UUID uuid, DiskRemoteInterfaceImpl drii) {
		activeDiskRemoteInterfaceImpls.put(uuid, drii);
	}
}
