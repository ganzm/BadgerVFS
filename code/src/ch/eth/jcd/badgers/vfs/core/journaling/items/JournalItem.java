package ch.eth.jcd.badgers.vfs.core.journaling.items;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public abstract class JournalItem {

	public abstract void replay(VFSDiskManager diskManager) throws VFSException;
}
