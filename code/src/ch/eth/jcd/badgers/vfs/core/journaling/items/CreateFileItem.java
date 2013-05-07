package ch.eth.jcd.badgers.vfs.core.journaling.items;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.VFSPathImpl;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class CreateFileItem extends JournalItem {

	private static final long serialVersionUID = 600164594871176343L;

	protected static final Logger LOGGER = Logger.getLogger(CreateFileItem.class);

	private final String absolutePath;

	public CreateFileItem(VFSPathImpl vfsPath) {
		this.absolutePath = vfsPath.getAbsolutePath();
	}

	public CreateFileItem(VFSEntry entry) {
		this.absolutePath = entry.getPath().getAbsolutePath();
	}

	@Override
	public void doReplay(VFSDiskManager diskManager) throws VFSException {
		LOGGER.debug("Journal - Create File " + absolutePath);
		VFSPath path = diskManager.createPath(absolutePath);
		path.createFile();
	}

	@Override
	public String toString() {
		return "CreateFileItem [absolutePath=" + absolutePath + "]";
	}
}
