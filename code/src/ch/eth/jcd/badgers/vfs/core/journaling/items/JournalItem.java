package ch.eth.jcd.badgers.vfs.core.journaling.items;

import java.io.Serializable;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.journaling.VFSJournaling;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public abstract class JournalItem implements Serializable {

	private static final long serialVersionUID = 6217683568058825388L;

	public void replay(VFSDiskManager diskManager) throws VFSException {
		// actions performed on the disk should not be added to the journal for obvious reasons
		diskManager.getJournaling().pauseJournaling(true);
		try {
			doReplay(diskManager);
		} finally {
			diskManager.getJournaling().pauseJournaling(false);
		}
	}

	public abstract void doReplay(VFSDiskManager diskManager) throws VFSException;

	public void onJournalAdd(VFSJournaling journaling) throws VFSException {
		// does nothing by default
	}

	public void beforeRmiTransport(VFSDiskManager diskManager) throws VFSException {
		// does nothing by default
	}

	public void afterRmiTransport(VFSDiskManager diskManager) {
		// does nothing by default
	}

	public void beforeLocalTransport(VFSDiskManager diskManager) throws VFSException {
		// does nothing by default
	}
}
