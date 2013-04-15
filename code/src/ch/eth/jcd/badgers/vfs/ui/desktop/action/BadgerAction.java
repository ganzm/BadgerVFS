package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public abstract class BadgerAction {

	// callback to be executed after the action, is allowed to be null
	private Callback callback;

	/**
	 * Performs this method decoupled of the GUI thread
	 * 
	 * @param diskManager
	 * @throws VFSException
	 */
	public abstract void runDiskAction(VFSDiskManager diskManager) throws VFSException;

	public Callback getCallback() {
		return callback;
	}

	public void setCallback(Callback cb) {
		this.callback = cb;
	}

}
