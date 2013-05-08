package ch.eth.jcd.badgers.vfs.core.journaling.items;

import java.io.Serializable;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.journaling.VFSJournaling;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public abstract class JournalItem implements Serializable {
	private static final Logger LOGGER = Logger.getLogger(JournalItem.class);

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

	@Override
	public abstract String toString();

	public abstract void doReplay(VFSDiskManager diskManager) throws VFSException;

	public void onJournalAdd(VFSJournaling journaling) throws VFSException {
		LOGGER.trace("do nothing on onJournalAdd");
	}

	public void beforeRmiTransport(VFSDiskManager diskManager) throws VFSException {
		LOGGER.trace("do nothing on beforeRmiTransport");
	}

	public void afterRmiTransport(VFSDiskManager diskManager) {
		LOGGER.trace("do nothing on afterRmiTransport");
	}

	public void beforeLocalTransport(VFSDiskManager diskManager) throws VFSException {
		LOGGER.trace("do nothing on beforeLocalTransport");
	}
}
