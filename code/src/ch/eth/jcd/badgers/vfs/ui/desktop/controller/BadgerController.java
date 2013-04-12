package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

public abstract class BadgerController {

	/**
	 * Reference to the view component
	 */
	protected final BadgerViewBase badgerView;

	public BadgerController(BadgerViewBase badgerView) {
		this.badgerView = badgerView;
	}

	protected void updateGUI() {
		badgerView.update();
	}
}
