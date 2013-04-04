package ch.eth.jcd.badgers.vfs.mock;

import ch.eth.jcd.badgers.vfs.core.interfaces.FindInFolderObserver;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;

public class MockedFindInFolderObserver implements FindInFolderObserver {

	@Override
	public void foundEntry(VFSPath entry) {
		System.out.println("Entry found: " + entry.toString());
	}

}
