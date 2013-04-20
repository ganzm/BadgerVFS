package ch.eth.jcd.badgers.vfs.ui.desktop.action;


public interface ActionObserver {

	void onActionFailed(BadgerAction action, Exception e);

	void onActionFinished(BadgerAction action);
}
