package ch.eth.jcd.badgers.vfs.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

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

	private static Logger LOGGER = Logger.getLogger(VFSDirectoryImpl.class);

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
	public boolean isDirectory() {
		return true;
	}

	@Override
	public List<VFSEntry> getChildren() throws VFSException {
		try {
			List<VFSEntry> result = new ArrayList<>();
			List<DirectoryEntryBlock> directoryEntryList = childTree.traverseTree(diskManager.getDirectorySectionHandler());

			for (DirectoryEntryBlock block : directoryEntryList) {
				result.add(createFromDirectoryEntryBlock(block));
			}

			return result;

		} catch (IOException e) {
			throw new VFSException(e);
		}

	}

	@Override
	public VFSEntryImpl getChildByName(String fileName) throws VFSException {

		try {
			List<DirectoryEntryBlock> directoryEntryList = childTree.traverseTree(diskManager.getDirectorySectionHandler());

			for (DirectoryEntryBlock block : directoryEntryList) {

				if (fileName.equals(block.getFileName())) {
					return createFromDirectoryEntryBlock(block);
				}
			}
			return null;
		} catch (IOException e) {
			throw new VFSException(e);
		}
	}

	private VFSEntryImpl createFromDirectoryEntryBlock(DirectoryEntryBlock block) throws IOException, VFSException {
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

	public void debugPrint() {
		try {
			String tree = childTree.dumpTreeToString(diskManager.getDirectorySectionHandler());
			LOGGER.debug("\n" + tree);
		} catch (Exception ex) {
			LOGGER.error("Error while debugPrint", ex);
		}
	}
}
