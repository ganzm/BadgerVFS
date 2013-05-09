package ch.eth.jcd.badgers.vfs.core.journaling.items;

import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.PathConflict;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class MoveToItem extends JournalItem {

	private static final long serialVersionUID = 788902060408180786L;

	protected static final Logger LOGGER = Logger.getLogger(MoveToItem.class);

	private String oldPathString;
	private String newPathString;

	public MoveToItem(String oldPathString, String newPathString) {
		this.oldPathString = oldPathString;
		this.newPathString = newPathString;
	}

	@Override
	public void doReplay(VFSDiskManager diskManager) throws VFSException {
		LOGGER.debug("Journal - Move Entry " + oldPathString + " to " + newPathString);
		VFSPath path = diskManager.createPath(oldPathString);
		VFSPath newPath = diskManager.createPath(newPathString);
		path.getVFSEntry().moveTo(newPath);
	}

	@Override
	public void doRevert(VFSDiskManager diskManager) throws VFSException {
		LOGGER.debug("Journal - Revert Move Entry " + oldPathString + " to " + newPathString);
		VFSPath path = diskManager.createPath(oldPathString);
		VFSPath newPath = diskManager.createPath(newPathString);
		newPath.getVFSEntry().moveTo(path);
	}

	@Override
	public void doReplayResolveConflics(VFSDiskManager diskManager, String conflictSuffix, List<PathConflict> conflicts) throws VFSException {
		for (PathConflict conflict : conflicts) {
			oldPathString = conflict.resolve(oldPathString);
			newPathString = conflict.resolve(newPathString);
		}

		VFSPath path = convertToNonflictingPath(diskManager, newPathString, conflictSuffix, conflicts);
		newPathString = path.getAbsolutePath();

		// do copy
		LOGGER.debug("Journal - Move Entry " + oldPathString + " to " + newPathString);
		VFSPath oldPpath = diskManager.createPath(oldPathString);
		VFSPath newPath = diskManager.createPath(newPathString);
		oldPpath.getVFSEntry().moveTo(newPath);
	}

	@Override
	public String toString() {
		return "MoveToItem [oldPathString=" + oldPathString + ", newPathString=" + newPathString + "]";
	}
}
