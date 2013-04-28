package ch.eth.jcd.badgers.vfs.ui.desktop.action.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;
import ch.eth.jcd.badgers.vfs.remote.streaming.RemoteInputStream;
import ch.eth.jcd.badgers.vfs.remote.streaming.RemoteInputStreamServer;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class LinkNewDiskAction extends RemoteAction {

	private final AdministrationRemoteInterface adminInterface;
	private final DiskConfiguration diskConfig;
	private static final Logger LOGGER = Logger.getLogger(LinkNewDiskAction.class);

	public LinkNewDiskAction(final ActionObserver actionObserver, final AdministrationRemoteInterface adminInterface, final DiskConfiguration diskConfig) {
		super(actionObserver);
		this.adminInterface = adminInterface;
		this.diskConfig = diskConfig;
	}

	@Override
	public void runRemoteAction() throws VFSException {
		RemoteInputStream ris = null;
		final LinkedDisk linkedDisk = new LinkedDisk(diskConfig.getHostFilePath().substring(diskConfig.getHostFilePath().lastIndexOf(File.separatorChar) + 1),
				diskConfig);
		try {
			ris = RemoteInputStreamServer.wrap(new FileInputStream(diskConfig.getHostFilePath()));
			adminInterface.linkNewDisk2(linkedDisk, ris);
		} catch (final RemoteException | FileNotFoundException e) {
			LOGGER.error(e);
			throw new VFSException(e);
		}
	}
}
