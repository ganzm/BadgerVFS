package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class CutAction extends DiskAction {

	private final List<VFSEntry> source;
	private final VFSEntry destinationFolder;

	public CutAction(ActionObserver actionObserver, List<VFSEntry> source, VFSEntry destinationFolder) {
		super(actionObserver);
		this.source = source;
		this.destinationFolder = destinationFolder;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		for (VFSEntry sourceEntry : source) {
			String destinationStringName = sourceEntry.getPath().getName();
			VFSPath destination = destinationFolder.getChildPath(destinationStringName);
			sourceEntry.moveTo(destination);
		}
	}

}
