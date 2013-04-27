package ch.eth.jcd.badgers.vfs.ui.desktop.action.remote;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.UUID;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.streaming.RemoteOutputStream;
import ch.eth.jcd.badgers.vfs.remote.streaming.RemoteOutputStreamServer;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class GetRemoteLinkedDiskAction extends RemoteAction {
	private final String localDiskPath;
	private final UUID diskId;
	private final AdministrationRemoteInterface adminInterface;
	private static final Logger LOGGER = Logger.getLogger(GetRemoteLinkedDiskAction.class);

	public GetRemoteLinkedDiskAction(final ActionObserver actionObserver, final AdministrationRemoteInterface adminInterface, final String localDiskPath,
			final UUID diskId) {
		super(actionObserver);
		this.adminInterface = adminInterface;
		this.localDiskPath = localDiskPath;
		this.diskId = diskId;
	}

	@Override
	public void runRemoteAction() throws VFSException {
		File localDisk;
		RemoteOutputStream ros = null;
		try {
			localDisk = new File(localDiskPath);
			ros = RemoteOutputStreamServer.wrap(new FileOutputStream(localDisk));
			adminInterface.getLinkedDisk(diskId, ros);
		} catch (final RemoteException | FileNotFoundException e) {
			LOGGER.error(e);
			throw new VFSException(e);
		} finally {
			if (ros != null) {
				try {
					ros.close();
				} catch (final IOException e) {
					LOGGER.trace(e);
				}
			}
		}
	}
}
