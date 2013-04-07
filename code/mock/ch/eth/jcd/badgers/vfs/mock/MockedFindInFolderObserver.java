package ch.eth.jcd.badgers.vfs.mock;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.FindInFolderCallback;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class MockedFindInFolderObserver implements FindInFolderCallback {
	private static final Logger LOGGER = Logger.getLogger(MockedFindInFolderObserver.class);

	@Override
	public void foundEntry(VFSPath path) {
		try {
			LOGGER.info("Entry found: " + path.getAbsolutePath());
		} catch (VFSException e) {
			LOGGER.error("Error while getting absolute path", e);
		}
	}

	@Override
	public boolean stopSearch(VFSPath currentDirectory) {
		try {
			LOGGER.info("current search dir" + currentDirectory.getAbsolutePath());
		} catch (VFSException e) {
			LOGGER.error("Error while getting absolute path", e);
		}
		// shall not stop the search
		return false;
	}

}
