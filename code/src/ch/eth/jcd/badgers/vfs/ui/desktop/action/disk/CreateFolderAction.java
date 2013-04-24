package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class CreateFolderAction extends DiskAction {

	private final VFSPath parentFolderPath;
	private final String name;
	private VFSEntry newFolder;

	public CreateFolderAction(ActionObserver actionObserver, VFSPath currentFolder, String name) {
		super(actionObserver);
		this.parentFolderPath = currentFolder;
		this.name = name;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		newFolder = parentFolderPath.getVFSEntry().getChildPath(name).createDirectory();

	}

	public VFSEntry getNewFolder() {
		return newFolder;
	}

}
