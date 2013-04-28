package ch.eth.jcd.badgers.vfs.core.journaling.items;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.VFSEntryImpl;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class DeleteEntryItem extends JournalItem {

	private static final long serialVersionUID = 8152410141037332312L;

	protected static final Logger LOGGER = Logger.getLogger(DeleteEntryItem.class);

	private final String absolutePath;

	public DeleteEntryItem(VFSEntryImpl vfsEntryImpl) {
		absolutePath = vfsEntryImpl.getPath().getAbsolutePath();
	}

	@Override
	public void doReplay(VFSDiskManager diskManager) throws VFSException {
		LOGGER.debug("Journal - Delete Entry " + absolutePath);
		VFSPath path = diskManager.createPath(absolutePath);
		path.getVFSEntry().delete();
	}
}
