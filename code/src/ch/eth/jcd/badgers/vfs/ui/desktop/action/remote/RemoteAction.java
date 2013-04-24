package ch.eth.jcd.badgers.vfs.ui.desktop.action.remote;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public abstract class RemoteAction extends AbstractBadgerAction {
	
	public RemoteAction(ActionObserver actionObserver) {
		super(actionObserver);
	}

	public abstract void runRemoteAction() throws VFSException;
}
