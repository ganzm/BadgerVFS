package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;

public abstract class BadgerAction implements Runnable {

	public void run() {

	}

	public abstract void runDiskAction(VFSDiskManager diskManager);

}
