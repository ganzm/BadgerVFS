package ch.eth.jcd.badgers.vfs.core.journaling.items;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class RenameEntryItem extends JournalItem {

	protected static final Logger LOGGER = Logger.getLogger(RenameEntryItem.class);

	private final String oldPath;
	private final String newName;

	public RenameEntryItem(String oldPath, String newName) {
		this.oldPath = oldPath;
		this.newName = newName;
	}

	@Override
	public void replay(VFSDiskManager diskManager) throws VFSException {
		LOGGER.debug("Journal - Rename " + oldPath + " to " + newName);
		VFSPath path = diskManager.createPath(oldPath);
		path.getVFSEntry().renameTo(newName);
	}
}
