package ch.eth.jcd.badgers.vfs.core.journaling.items;

import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.VFSEntryImpl;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.PathConflict;
import ch.eth.jcd.badgers.vfs.core.journaling.VFSJournaling;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class DeleteEntryItem extends JournalItem {

	private static final long serialVersionUID = 8152410141037332312L;

	protected static final Logger LOGGER = Logger.getLogger(DeleteEntryItem.class);

	/**
	 * Absolute Path to the file or directory to delete
	 */
	private String absolutePath;

	/**
	 * That's where we put our File/Folder when deleting so we are able to undo deletion
	 */
	private String absoluteJournalPath;

	public DeleteEntryItem(VFSEntryImpl vfsEntryImpl) {
		absolutePath = vfsEntryImpl.getPath().getAbsolutePath();
	}

	@Override
	public void doReplay(VFSDiskManager diskManager) throws VFSException {
		LOGGER.debug("Journal - Delete Entry " + absolutePath);
		VFSPath path = diskManager.createPath(absolutePath);
		path.getVFSEntry().delete();
	}

	@Override
	public String toString() {
		return "DeleteEntryItem [absolutePath=" + absolutePath + "]";
	}

	@Override
	public void onJournalAdd(VFSJournaling journaling) throws VFSException {
		VFSPath journalPath = journaling.copyFileToJournal(absolutePath);
		absoluteJournalPath = journalPath.getAbsolutePath();
	}

	@Override
	public void doRevert(VFSDiskManager diskManager) throws VFSException {
		VFSPath userSpacePath = diskManager.createPath(absolutePath);
		VFSPath journalPath = diskManager.createPath(absoluteJournalPath);

		VFSEntry entryInJournal = journalPath.getVFSEntry();
		entryInJournal.copyTo(userSpacePath);
	}

	@Override
	public void doReplayResolveConflics(VFSDiskManager diskManager, String conflictSuffix, List<PathConflict> conflicts) throws VFSException {
		for (PathConflict conflict : conflicts) {
			absolutePath = conflict.resolve(absolutePath);
		}

		doReplay(diskManager);
	}
}
