package ch.eth.jcd.badgers.vfs.core.journaling.items;

import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.PathConflict;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class RenameEntryItem extends JournalItem {

	private static final long serialVersionUID = 4354596804041585526L;

	protected static final Logger LOGGER = Logger.getLogger(RenameEntryItem.class);

	private final String oldPath;
	private final String newName;

	public RenameEntryItem(String oldPath, String newName) {
		this.oldPath = oldPath;
		this.newName = newName;
	}

	@Override
	public void doReplay(VFSDiskManager diskManager) throws VFSException {
		LOGGER.debug("Journal - Rename " + oldPath + " to " + newName);
		VFSPath path = diskManager.createPath(oldPath);
		path.getVFSEntry().renameTo(newName);
	}

	@Override
	public void doRevert(VFSDiskManager diskManager) throws VFSException {
		LOGGER.debug("Journal - Revert Rename " + oldPath + " to " + newName);
		VFSPath path = diskManager.createPath(newName);
		path.getVFSEntry().renameTo(oldPath);
	}

	@Override
	public void doReplayResolveConflics(VFSDiskManager diskManager, String conflictSuffix, List<PathConflict> conflicts) throws VFSException {

	}

	@Override
	public String toString() {
		return "RenameEntryItem [oldPath=" + oldPath + ", newName=" + newName + "]";
	}
}
