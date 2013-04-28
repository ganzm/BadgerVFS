package ch.eth.jcd.badgers.vfs.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
import ch.eth.jcd.badgers.vfs.core.directory.DirectoryBlock;
import ch.eth.jcd.badgers.vfs.core.directory.DirectoryChildTree;
import ch.eth.jcd.badgers.vfs.core.directory.DirectoryEntryBlock;
import ch.eth.jcd.badgers.vfs.core.interfaces.FindInFolderCallback;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.model.SearchParameter;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.exception.VFSInvalidLocationExceptionException;

/**
 * $Id$
 * 
 * 
 * Represents a single directory on the virtual file system
 * 
 */
public class VFSDirectoryImpl extends VFSEntryImpl {

	private static final Logger LOGGER = Logger.getLogger(VFSDirectoryImpl.class);

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
			if (directoryEntry == null) {
				// child not found
				return null;
			}

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
		throw new VFSException("getOutputStream() does not work on directories");
	}

	@Override
	public void copyTo(VFSPath newLocation) throws VFSException {
		if (newLocation.exists()) {
			throw new VFSException("Copy failed - file already exist " + newLocation.getAbsolutePath());
		}
		LOGGER.info("Copy Folder " + path.getAbsolutePath() + " to " + newLocation.getAbsolutePath());

		// list children before creating the target directory
		// avoid bug where you copy a folder into itself
		List<VFSEntry> childEntries = getChildren();

		VFSEntry newDir = newLocation.createDirectory();

		// copy child entries
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

	@Override
	public void findInFolder(String fileName, FindInFolderCallback observer) throws VFSException {
		SearchParameter param = new SearchParameter();
		param.setSearchString(fileName);
		findInFolder(param, observer);
	}

	@Override
	public void findInFolder(SearchParameter searchParameter, FindInFolderCallback observer) throws VFSException {
		privateFindInFolder(searchParameter, observer);
	}

	/**
	 * @see this{@link #findInFolder(SearchParameter, FindInFolderCallback)}
	 * @return false if we want to go ahead searching, true if we want to cancel search
	 */
	public boolean privateFindInFolder(SearchParameter searchParameter, FindInFolderCallback observer) throws VFSException {
		LOGGER.debug("Find in Fold " + path.getAbsolutePath());

		if (observer.stopSearch(path)) {
			LOGGER.debug("API User wanted to stop searching");
			return true;
		}

		List<DirectoryEntryBlock> toDeepSearch = new ArrayList<DirectoryEntryBlock>();
		List<DirectoryEntryBlock> children = childTree.traverseTree(diskManager.getDirectorySectionHandler());
		for (DirectoryEntryBlock child : children) {

			if (searchParameter.matches(child.getFileName())) {
				// found it
				LOGGER.debug("Found File " + child.getFileName() + " in Folder " + this.getPath().getAbsolutePath());

				VFSPath path = this.getChildPath(child.getFileName());

				// notify observer
				observer.foundEntry(path);
			}

			if (searchParameter.isIncludeSubFolders() && child.isFolderEntryBlock()) {
				// remember folder to search only if the user want us to do deep search
				toDeepSearch.add(child);
			}
		}

		for (DirectoryEntryBlock entry : toDeepSearch) {
			VFSPath path = this.getChildPath(entry.getFileName());
			VFSDirectoryImpl directory = (VFSDirectoryImpl) path.getVFSEntry();
			if (directory.privateFindInFolder(searchParameter, observer)) {
				// search was canceled, immediately return
				return true;
			}
		}

		return false;
	}
}
