package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public abstract class BadgerAction {

	/**
	 * Performs this method decoupled of the GUI thread
	 * 
	 * @param diskManager
	 * @throws VFSException
	 */
	public abstract void runDiskAction(VFSDiskManager diskManager) throws VFSException;

}
