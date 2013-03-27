package ch.eth.jcd.badgers.vfs.core;

import java.util.List;

import ch.eth.jcd.badgers.vfs.core.directory.DirectoryBlock;
import ch.eth.jcd.badgers.vfs.core.directory.DirectoryEntryBlock;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

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

	public DirectoryChildTree getChildTree() {
		return childTree;
	}

	@Override
	public List<VFSEntry> getChildren() {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VFSEntry getChildByName(String fileName) throws VFSException {
		List<DirectoryEntryBlock> directoryEntryList = childTree.traverseTree(diskManager.getDirectorySectionHandler());

		for (DirectoryEntryBlock block : directoryEntryList) {

			if (fileName.equals(block.getFileName())) {

				VFSPathImpl path = (VFSPathImpl) getChildPath(block.getFileName());

				VFSEntryImpl entry;
				if (block.isFolderEntryBlock()) {
					entry = new VFSDirectoryImpl(diskManager, path);
				} else {
					entry = new VFSFileImpl(diskManager, path);
				}

				return entry;
			}
		}
		return null;
	}
}
