package ch.eth.jcd.badgers.vfs.ui.desktop.action.remote;

import java.rmi.RemoteException;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class CreateNewDiskAction extends RemoteAction {

	private final AdministrationRemoteInterface adminInterface;
	private final LinkedDisk linkedDiskPrototype;

	public CreateNewDiskAction(final ActionObserver actionObserver, final AdministrationRemoteInterface adminInterface, final LinkedDisk linkedDiskPrototype) {
		super(actionObserver);
		this.linkedDiskPrototype = linkedDiskPrototype;
		this.adminInterface = adminInterface;
	}

	@Override
	public void runRemoteAction() throws VFSException {
		try {
			adminInterface.createNewDisk(linkedDiskPrototype);
		} catch (final RemoteException e) {
			throw new VFSException(e);
		}
	}

}
