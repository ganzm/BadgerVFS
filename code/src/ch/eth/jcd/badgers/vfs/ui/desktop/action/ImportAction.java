package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class ImportAction extends BadgerAction {

	private final String hostFsSourcePath;
	private final VFSPath destinationPath;

	public ImportAction(String hostFsSourcePath, VFSPath destinationPath) {
		this.hostFsSourcePath = hostFsSourcePath;
		this.destinationPath = destinationPath;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		throw new UnsupportedOperationException("TODO");
	}
}
