package ch.eth.jcd.badgers.vfs.core;

import ch.eth.jcd.badgers.vfs.core.directory.DirectoryBlock;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;

/**
 * $Id$
 * 
 * 
 * TODO describe VFSDirectoryImpl
 * 
 */
public class VFSDirectoryImpl extends VFSEntryImpl {

	private DirectoryChildTree childTree;

	protected VFSDirectoryImpl(VFSDiskManagerImpl diskManager, VFSPath path) {
		super(diskManager, path);
	}

	public void setDirectoryBlock(DirectoryBlock directoryBlock) {
		childTree = new DirectoryChildTree(directoryBlock);
	}

}
