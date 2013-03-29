package ch.eth.jcd.badgers.vfs.core;

import java.io.IOException;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
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

	protected VFSDirectoryImpl(VFSDiskManagerImpl diskManager, VFSPath path, DataBlock firstDataBlock, DirectoryBlock directoryBlock) {
		super(diskManager, path, firstDataBlock);
		childTree = new DirectoryChildTree(directoryBlock);
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
	public VFSEntryImpl getChildByName(String fileName) throws VFSException {

		try {
			List<DirectoryEntryBlock> directoryEntryList = childTree.traverseTree(diskManager.getDirectorySectionHandler());

			for (DirectoryEntryBlock block : directoryEntryList) {

				if (fileName.equals(block.getFileName())) {

					VFSPathImpl path = (VFSPathImpl) getChildPath(block.getFileName());

					DataBlock firstDataBlock = diskManager.getDataSectionHandler().loadDataBlock(block.getDataBlockLocation());

					VFSEntryImpl entry;
					if (block.isFolderEntryBlock()) {
						DirectoryBlock directoryBlock = diskManager.getDirectorySectionHandler().loadDirectoryBlock(block.getDirectoryEntryNodeLocation());
						entry = new VFSDirectoryImpl(diskManager, path, firstDataBlock, directoryBlock);

					} else {
						entry = new VFSFileImpl(diskManager, path, firstDataBlock);
					}

					return entry;
				}
			}
			return null;
		} catch (IOException e) {
			throw new VFSException(e);
		}
	}
}
