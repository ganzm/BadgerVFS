package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.DiskAction;

public interface WorkLoadIndicator {

	void actionEnqueued(DiskAction action);

	void actionFinished(DiskAction action);

	void dispose();

}
