package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

public abstract class BadgerController {

	/**
	 * Reference to the view component
	 */
	private final BadgerViewBase badgerView;

	public BadgerController(final BadgerViewBase badgerView) {
		this.badgerView = badgerView;
	}

	protected void updateGUI() {
		badgerView.update();
	}

	protected BadgerViewBase getView() {
		return badgerView;
	}
}
