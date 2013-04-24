package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public abstract class DiskAction extends AbstractBadgerAction {

	public DiskAction(final ActionObserver actionObserver) {
		super(actionObserver);
	}

	/**
	 * Performs this method decoupled of the GUI thread
	 * 
	 * @param diskManager
	 * @throws VFSException
	 */
	public abstract void runDiskAction(VFSDiskManager diskManager) throws VFSException;

}
