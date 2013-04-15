package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import ch.eth.jcd.badgers.vfs.core.VFSImporter;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class ImportAction extends BadgerAction {

	private final String hostFsSourcePath;
	private final String destinationPath;

	public ImportAction(ActionObserver actionObserver, String hostFsSourcePath, String destinationPath) {
		super(actionObserver);
		this.hostFsSourcePath = hostFsSourcePath;
		this.destinationPath = destinationPath;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		VFSPath path = diskManager.createPath(destinationPath);
		VFSImporter importer = new VFSImporter();
		importer.importFileOrFolder(hostFsSourcePath, path);
	}
}
