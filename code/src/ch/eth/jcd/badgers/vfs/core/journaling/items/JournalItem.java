package ch.eth.jcd.badgers.vfs.core.journaling.items;

import java.io.Serializable;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public abstract class JournalItem implements Serializable {

	private static final long serialVersionUID = 6217683568058825388L;

	public void replay(VFSDiskManager diskManager) throws VFSException {
		// actions performed on the disk should not be added to the journal for oviousreasons
		diskManager.pauseJournaling(true);
		try {
			doReplay(diskManager);
		} finally {
			diskManager.pauseJournaling(false);
		}
	}

	public abstract void doReplay(VFSDiskManager diskManager) throws VFSException;
}
