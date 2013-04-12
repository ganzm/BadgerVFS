package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import ch.eth.jcd.badgers.vfs.exception.VFSException;

public interface ActionObserver {

	void onActionFailed(BadgerAction action, VFSException e);

	void onActionFinished(BadgerAction action);
}
