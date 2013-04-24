package ch.eth.jcd.badgers.vfs.ui.desktop.action;

public interface ActionObserver {

	void onActionFailed(AbstractBadgerAction action, Exception e);

	void onActionFinished(AbstractBadgerAction action);
}
