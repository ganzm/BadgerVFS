package ch.eth.jcd.badgers.vfs.core.journaling.items;

import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.PathConflict;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class RenameEntryItem extends JournalItem {

	private static final long serialVersionUID = 4354596804041585526L;

	protected static final Logger LOGGER = Logger.getLogger(RenameEntryItem.class);

	private final String oldPathString;
	private String newName;

	private String oldName;
	private String newPathString;

	public RenameEntryItem(String oldPath, String newName) {
		this.oldPathString = oldPath;
		this.newName = newName;
	}

	@Override
	public void doReplay(VFSDiskManager diskManager) throws VFSException {
		LOGGER.debug("Journal - Rename " + oldPathString + " to " + newName);
		VFSPath path = diskManager.createPath(oldPathString);
		VFSEntry entry = path.getVFSEntry();
		oldName = entry.getPath().getName();
		entry.renameTo(newName);
		newPathString = entry.getPath().getAbsolutePath();
	}

	@Override
	public void doRevert(VFSDiskManager diskManager) throws VFSException {
		LOGGER.debug("Journal - Revert Rename " + oldPathString + " to " + newName);

		VFSPath oldPath = diskManager.createPath(oldPathString);
		VFSPath newPath = oldPath.renameTo(newName);
		newPathString = newPath.getAbsolutePath();

		oldName = oldPath.getName();

		VFSEntry newEntry = newPath.getVFSEntry();
		newEntry.renameTo(oldName);
	}

	@Override
	public void doReplayResolveConflics(VFSDiskManager diskManager, String conflictSuffix, List<PathConflict> conflicts) throws VFSException {
		VFSPath path = super.convertToNonflictingPath(diskManager, newPathString, conflictSuffix, conflicts);

		newPathString = path.getAbsolutePath();
		newName = path.getName();

		VFSPath old = diskManager.createPath(oldPathString);
		old.getVFSEntry().renameTo(newName);
	}

	@Override
	public String toString() {
		return "RenameEntryItem [oldPath=" + oldPathString + ", newName=" + newName + "]";
	}
}
