package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

/**
 * gemäss Upload Pull SequenzDiagramm
 * 
 * Versucht 1x lokale Änderungen auf den SynchServer hochzuladen
 * 
 * 
 * If this action fails
 * 
 * 
 */
public class UploadLocalChanges extends DiskAction {

	public UploadLocalChanges(ActionObserver actionObserver) {
		super(actionObserver);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		// TODO Auto-generated method stub

	}

}
