package ch.eth.jcd.badgers.vfs.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
import ch.eth.jcd.badgers.vfs.core.directory.DirectoryBlock;
import ch.eth.jcd.badgers.vfs.core.directory.DirectoryEntryBlock;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.exception.VFSInvalidLocationExceptionException;

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

	protected VFSDirectoryImpl(VFSDiskManagerImpl diskManager, VFSPathImpl path, DataBlock firstDataBlock, DirectoryBlock directoryBlock) {
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
			DirectoryEntryBlock directoryEntry = getChildDirectoryEntryBlockByName(fileName);
			return createFromDirectoryEntryBlock(directoryEntry);
		} catch (IOException e) {
			throw new VFSException(e);
		}
	}

	protected DirectoryEntryBlock getChildDirectoryEntryBlockByName(String fileName) throws VFSException {

		List<DirectoryEntryBlock> directoryEntryList = childTree.traverseTree(diskManager.getDirectorySectionHandler());

		for (DirectoryEntryBlock block : directoryEntryList) {

			if (fileName.equals(block.getFileName())) {
				return block;
			}
		}

		return null;
	}

	protected void renameDirectoryEntryBlock(String oldFileName, String newFileName) throws VFSException, IOException {
		DirectoryEntryBlock removedBlock = childTree.remove(diskManager.getDirectorySectionHandler(), oldFileName);

		// rename entry
		removedBlock.setFileName(newFileName);

		// and insert into the tree
		childTree.insert(diskManager.getDirectorySectionHandler(), removedBlock);

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
		} catch (VFSException ex) {
			LOGGER.error("Error while debugPrint", ex);
		}
	}

	public boolean performTreeSanityCheck(StringBuffer buf) throws VFSInvalidLocationExceptionException, VFSException {
		return childTree.performTreeSanityCheck(diskManager.getDirectorySectionHandler(), buf);
	}

	protected void deleteChild(VFSEntryImpl entry) throws VFSException, IOException {
		String filePath = entry.getPath().getAbsolutePath();
		String fileName = entry.getPath().getName();

		LOGGER.info("Deleting... " + filePath);

		if (entry.isDirectory()) {
			VFSDirectoryImpl directoryEntry = ((VFSDirectoryImpl) entry);

			// delete directory content first
			List<VFSEntry> childEntries = entry.getChildren();
			for (VFSEntry childEntry : childEntries) {
				directoryEntry.deleteChild((VFSEntryImpl) childEntry);
			}

			// delete Directory tree structure
			long directoryRootBlockLocation = directoryEntry.childTree.getRootBlockLocation();
			diskManager.getDirectorySectionHandler().freeDirectoryBlock(directoryRootBlockLocation);
		}

		// remove this from parent directory tree structure
		childTree.remove(diskManager.getDirectorySectionHandler(), fileName);
		entry.deleteDataBlocks();

		LOGGER.info("Deleting DONE " + filePath);
	}

	@Override
	public InputStream getInputStream() throws VFSException {
		throw new VFSException("getInputStream() does not work on directories");
	}

	@Override
	public OutputStream getOutputStream(int writeMode) throws VFSException {
		throw new VFSException("getInputStream() does not work on directories");
	}

	@Override
	public void copyTo(VFSPath newLocation) throws VFSException {
		if (newLocation.exists()) {
			throw new VFSException("Copy failed - file already exist " + newLocation.getAbsolutePath());
		}
		LOGGER.info("Copy Folder " + path.getAbsolutePath() + " to " + newLocation.getAbsolutePath());

		VFSEntry newDir = newLocation.createDirectory();

		// copy child entries
		List<VFSEntry> childEntries = getChildren();
		for (VFSEntry child : childEntries) {
			VFSPath newChildLocation = newDir.getChildPath(child.getPath().getName());
			child.copyTo(newChildLocation);
		}
	}

	public void moveDirectoryEntry(String oldName, VFSDirectoryImpl newParentDirectory, String newName) throws VFSException {
		try {
			DirectoryEntryBlock removedEntry = childTree.remove(diskManager.getDirectorySectionHandler(), oldName);
			removedEntry.setFileName(newName);
			newParentDirectory.childTree.insert(newParentDirectory.diskManager.getDirectorySectionHandler(), removedEntry);
		} catch (IOException e) {
			throw new VFSException(e);
		}
	}

	@Override
	public String toString() {
		return "Directory to " + path;
	}
}
