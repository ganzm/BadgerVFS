package ch.eth.jcd.badgers.vfs.core.journaling.items;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class MoveToItem extends JournalItem {

	private static final long serialVersionUID = 788902060408180786L;

	protected static final Logger LOGGER = Logger.getLogger(MoveToItem.class);

	private final String oldPathString;
	private final String newPathString;

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

}
