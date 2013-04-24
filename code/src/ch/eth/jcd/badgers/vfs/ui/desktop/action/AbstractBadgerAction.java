package ch.eth.jcd.badgers.vfs.ui.desktop.action;

public class AbstractBadgerAction {
	/**
	 * callback to be executed after the action, is allowed to be null
	 */
	private final ActionObserver actionObserver;

	public AbstractBadgerAction(final ActionObserver actionObserver) {
		this.actionObserver = actionObserver;
	}

	public ActionObserver getActionObserver() {
		return actionObserver;
	}

	public String getActionName() {
		return this.getClass().getSimpleName();
	}

	public boolean isProgressIndicationSupported() {
		return false;
	}

	public int getCurrentProgress() {
		return 0;
	}

	public int getMaxProgress() {
		return 0;
	}

	/**
	 * returns true if a specific specific action should lock the gui
	 */
	public boolean needsToLockGui() {
		return true;
	}
}
