package ch.eth.jcd.badgers.vfs.ui.desktop.action.remote;

import java.io.File;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.AdministrationRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.interfaces.DiskRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.LinkedDisk;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class LinkNewDiskAction extends RemoteAction {

	private final AdministrationRemoteInterface adminInterface;
	private final DiskConfiguration diskConfig;
	private final Journal journal;
	private DiskRemoteInterface result;
	private static final Logger LOGGER = Logger.getLogger(LinkNewDiskAction.class);

	public LinkNewDiskAction(final ActionObserver actionObserver, final AdministrationRemoteInterface adminInterface, final DiskConfiguration diskConfig,
			Journal journal) {
		super(actionObserver);
		this.adminInterface = adminInterface;
		this.diskConfig = diskConfig;
		this.journal = journal;
	}

	@Override
	public void runRemoteAction() throws VFSException {
		final String displayName = diskConfig.getHostFilePath().substring(diskConfig.getHostFilePath().lastIndexOf(File.separatorChar) + 1);
		final LinkedDisk linkedDisk = new LinkedDisk(displayName, diskConfig);
		try {
			result = adminInterface.linkNewDisk(linkedDisk, journal);
		} catch (final RemoteException e) {
			LOGGER.error(e);
			throw new VFSException(e);
		}
	}

	public DiskRemoteInterface getResult() {
		return result;
	}
}
