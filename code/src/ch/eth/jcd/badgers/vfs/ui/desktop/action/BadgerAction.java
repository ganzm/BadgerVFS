package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public abstract class BadgerAction {

	public abstract void runDiskAction(VFSDiskManager diskManager) throws VFSException;

}
