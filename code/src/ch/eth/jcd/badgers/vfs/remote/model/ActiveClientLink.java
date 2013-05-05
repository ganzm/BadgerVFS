package ch.eth.jcd.badgers.vfs.remote.model;

import ch.eth.jcd.badgers.vfs.remote.ifimpl.AdministrationRemoteInterfaceImpl;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.sync.server.ClientLink;

public class ActiveClientLink {

	private final ClientLink clientLink;
	private AdministrationRemoteInterface remoteIf;
	private AdministrationRemoteInterfaceImpl remoteifImpl;

	public ActiveClientLink(ClientLink clientLink) {
		this.clientLink = clientLink;
	}

	public void setRmiIf(AdministrationRemoteInterfaceImpl remoteifImpl, AdministrationRemoteInterface remoteIf) {
		this.remoteifImpl = remoteifImpl;
		this.remoteIf = remoteIf;
	}

	public ClientLink getClientLink() {
		return clientLink;
	}

	public AdministrationRemoteInterface getRemoteIf() {
		return remoteIf;
	}

	public AdministrationRemoteInterfaceImpl getRemoteifImpl() {
		return remoteifImpl;
	}
}
