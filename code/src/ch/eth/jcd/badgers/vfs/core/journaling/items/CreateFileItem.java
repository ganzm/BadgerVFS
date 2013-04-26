package ch.eth.jcd.badgers.vfs.core.journaling.items;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.VFSPathImpl;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class CreateFileItem extends JournalItem {

	protected static final Logger LOGGER = Logger.getLogger(CreateFileItem.class);

	private final String absolutePath;

	public CreateFileItem(VFSPathImpl vfsPath) {
		absolutePath = vfsPath.getAbsolutePath();
	}

	@Override
	public void replay(VFSDiskManager diskManager) throws VFSException {
		LOGGER.debug("Journal - Create File " + absolutePath);
		VFSPath path = diskManager.createPath(absolutePath);
		path.createFile();
	}
}
