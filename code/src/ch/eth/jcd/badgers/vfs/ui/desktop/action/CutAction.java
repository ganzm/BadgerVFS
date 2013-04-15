package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class CutAction extends BadgerAction {

	private final VFSEntry source;
	private final VFSEntry destinationFolder;

	public CutAction(ActionObserver actionObserver, VFSEntry source, VFSEntry destinationFolder) {
		super(actionObserver);
		this.source = source;
		this.destinationFolder = destinationFolder;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		String destinationStringName = source.getPath().getName();
		VFSPath destination = destinationFolder.getChildPath(destinationStringName);
		source.moveTo(destination);
	}

}
