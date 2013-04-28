package ch.eth.jcd.badgers.vfs.core.journaling.items;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.VFSPathImpl;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class CreateDirectoryItem extends JournalItem {

	private static final long serialVersionUID = 3004321171413930137L;

	protected static final Logger LOGGER = Logger.getLogger(CreateDirectoryItem.class);

	private final String absolutePath;

	public CreateDirectoryItem(VFSPathImpl vfsPath) {
		absolutePath = vfsPath.getAbsolutePath();
	}

	public CreateDirectoryItem(VFSEntry entry) {
		this.absolutePath = entry.getPath().getAbsolutePath();
	}

	@Override
	public void replay(VFSDiskManager diskManager) throws VFSException {
		LOGGER.debug("Journal - Create Directory " + absolutePath);
		VFSPath path = diskManager.createPath(absolutePath);
		path.createDirectory();
	}
}
