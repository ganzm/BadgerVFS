package ch.eth.jcd.badgers.vfs.core.journaling.items;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.PathConflict;
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

	public void revert(VFSDiskManager diskManager) throws VFSException {
		// actions performed on the disk should not be added to the journal for obvious reasons
		diskManager.getJournaling().pauseJournaling(true);
		try {
			doRevert(diskManager);
		} finally {
			diskManager.getJournaling().pauseJournaling(false);
		}
	}

	public void replayResolveConflics(VFSDiskManager diskManager, String conflictSuffix, List<PathConflict> conflicts) throws VFSException {
		diskManager.getJournaling().pauseJournaling(true);
		try {
			doReplayResolveConflics(diskManager, conflictSuffix, conflicts);
		} finally {
			diskManager.getJournaling().pauseJournaling(false);
		}
	}

	public abstract void doRevert(VFSDiskManager diskManager) throws VFSException;

	public abstract void doReplayResolveConflics(VFSDiskManager diskManager, String conflictSuffix, List<PathConflict> conflicts) throws VFSException;

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

	protected VFSPath convertToNonflictingPath(VFSDiskManager diskManager, String absolutePath, String conflictSuffix, List<PathConflict> conflicts)
			throws VFSException {

		String currentPath = absolutePath;
		VFSPath path = diskManager.createPath(currentPath);
		int i = 0;
		while (path.exists()) {
			currentPath = absolutePath + conflictSuffix + i;
			path = diskManager.createPath(currentPath);
			i++;
		}

		if (!currentPath.equals(absolutePath)) {
			conflicts.add(new PathConflict(absolutePath, currentPath));
			absolutePath = currentPath;
		}

		return path;
	}

}
