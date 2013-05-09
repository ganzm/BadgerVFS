package ch.eth.jcd.badgers.vfs.core.journaling.items;

import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.VFSPathImpl;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.PathConflict;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class CreateDirectoryItem extends JournalItem {

	private static final long serialVersionUID = 3004321171413930137L;

	protected static final Logger LOGGER = Logger.getLogger(CreateDirectoryItem.class);

	private String absolutePath;

	public CreateDirectoryItem(VFSPathImpl vfsPath) {
		absolutePath = vfsPath.getAbsolutePath();
	}

	public CreateDirectoryItem(VFSEntry entry) {
		this.absolutePath = entry.getPath().getAbsolutePath();
	}

	@Override
	public void doReplay(VFSDiskManager diskManager) throws VFSException {
		LOGGER.debug("Journal - Create Directory " + absolutePath);
		VFSPath path = diskManager.createPath(absolutePath);
		path.createDirectory();
	}

	@Override
	public void doRevert(VFSDiskManager diskManager) throws VFSException {
		LOGGER.debug("Journal - Revert Create Directory " + absolutePath);
		VFSPath path = diskManager.createPath(absolutePath);
		VFSEntry directory = path.getVFSEntry();
		directory.delete();
	}

	@Override
	public void doReplayResolveConflics(VFSDiskManager diskManager, String conflictSuffix, List<PathConflict> conflicts) throws VFSException {
		LOGGER.debug("Journal - Revert Create Directory " + absolutePath);

		for (PathConflict conflict : conflicts) {
			absolutePath = conflict.resolve(absolutePath);
		}

		VFSPath path = convertToNonflictingPath(diskManager, absolutePath, conflictSuffix, conflicts);
		absolutePath = path.getAbsolutePath();

		LOGGER.debug("Journal - Revert Create Directory " + absolutePath);
		path.createDirectory();

	}

	@Override
	public String toString() {
		return "CreateDirectoryItem [absolutePath=" + absolutePath + "]";
	}

}
