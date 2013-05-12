package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.DiskAction;

public class WorkLoadIndicatorPassive implements WorkLoadIndicator {

	@Override
	public void actionEnqueued(DiskAction action) {
	}

	@Override
	public void actionFinished(DiskAction action) {
	}

	@Override
	public void dispose() {
	}
}
