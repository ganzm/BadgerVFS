package ch.eth.jcd.badgers.vfs.ui.desktop.action.remote;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.LoginRemoteInterface;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class ConnectAction extends RemoteAction {
	private static final Logger LOGGER = Logger.getLogger(ConnectAction.class);

	private final String remoteHost;

	private LoginRemoteInterface loginInterface;

	public ConnectAction(String remoteHost, ActionObserver actionObserver) {
		super(actionObserver);
		this.remoteHost = remoteHost;
	}

	@Override
	public void runRemoteAction() throws VFSException {
		try {
			LOGGER.debug("Get RMI Registry from " + remoteHost);
			final Registry registry = LocateRegistry.getRegistry(remoteHost);
			LOGGER.debug("Get RMI Login Interface from " + remoteHost);
			loginInterface = (LoginRemoteInterface) registry.lookup(LoginRemoteInterface.LOGIN_INTERFACE_KEY);
			LOGGER.debug("Connected to " + remoteHost);
		} catch (final Exception e) {
			throw new VFSException(e);
		}
	}

	public LoginRemoteInterface getLoginInterface() {
		return loginInterface;
	}

}
