package ch.eth.jcd.badgers.vfs.core.journaling.items;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.VFSFileImpl;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class ModifyFileItem extends JournalItem {

	protected static final Logger LOGGER = Logger.getLogger(ModifyFileItem.class);

	private final String absolutePath;

	public ModifyFileItem(VFSFileImpl vfsFileImpl) {
		this.absolutePath = vfsFileImpl.getPath().getAbsolutePath();
	}

	@Override
	public void replay(VFSDiskManager diskManager) throws VFSException {
		LOGGER.debug("Journal - Modify File " + absolutePath);
		LOGGER.debug("TODO");
	}
}
