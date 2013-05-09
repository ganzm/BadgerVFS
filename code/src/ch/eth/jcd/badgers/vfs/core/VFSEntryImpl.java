package ch.eth.jcd.badgers.vfs.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
import ch.eth.jcd.badgers.vfs.core.directory.DirectoryBlock;
import ch.eth.jcd.badgers.vfs.core.directory.DirectoryEntryBlock;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.items.CreateDirectoryItem;
import ch.eth.jcd.badgers.vfs.core.journaling.items.CreateFileItem;
import ch.eth.jcd.badgers.vfs.core.journaling.items.DeleteEntryItem;
import ch.eth.jcd.badgers.vfs.core.journaling.items.MoveToItem;
import ch.eth.jcd.badgers.vfs.core.journaling.items.RenameEntryItem;
import ch.eth.jcd.badgers.vfs.exception.VFSDuplicatedEntryException;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;

public abstract class VFSEntryImpl implements VFSEntry {

	private static final Logger LOGGER = Logger.getLogger(VFSEntryImpl.class);

	protected final VFSDiskManagerImpl diskManager;

	protected VFSPathImpl path;

	protected long firstDataBlockLocation = -1;

	/**
	 * creates a new
	 * 
	 * @param path
	 */
	protected VFSEntryImpl(VFSDiskManagerImpl diskManager, VFSPathImpl path, long firstDataBlockLocation) {
		this.diskManager = diskManager;
		this.path = path;
		this.firstDataBlockLocation = firstDataBlockLocation;
	}

	/**
	 * 
	 * @param vfsPath
	 * @return
	 * @throws VFSException
	 * @throws IOException
	 */
	protected static VFSDirectoryImpl createNewDirectory(VFSDiskManagerImpl diskManager, VFSPathImpl vfsPath) throws VFSException, IOException {

		DataBlock dataBlock = null;
		DirectoryBlock directoryBlock = null;
		try {
			dataBlock = diskManager.getDataSectionHandler().allocateNewDataBlock(true);
			directoryBlock = diskManager.getDirectorySectionHandler().allocateNewDirectoryBlock();

			insertEntryIntoParentFolder(diskManager, vfsPath, dataBlock.getLocation(), directoryBlock);

			VFSDirectoryImpl entry = new VFSDirectoryImpl(diskManager, vfsPath, dataBlock.getLocation(), directoryBlock);

			diskManager.addJournalItem(new CreateDirectoryItem(vfsPath));

			return entry;
		} catch (IOException ex) {

			// try to revert changes performed on the file system
			if (dataBlock != null) {
				diskManager.getDataSectionHandler().freeDataBlock(dataBlock);
			}

			if (directoryBlock != null) {
				diskManager.getDirectorySectionHandler().freeDirectoryBlock(directoryBlock);
			}

			throw ex;
		}
	}

	/**
	 * Creates a new File by using a predefined DataBlock as first block of the file
	 * 
	 * @param diskManager
	 * @param vfsPath
	 * @param dataBlock
	 * @return
	 * @throws VFSException
	 * @throws IOException
	 */
	protected static VFSFileImpl createNewFile(VFSDiskManagerImpl diskManager, VFSPathImpl vfsPath, long dataBlockLocation) throws VFSException, IOException {

		insertEntryIntoParentFolder(diskManager, vfsPath, dataBlockLocation, null);

		VFSFileImpl entry = new VFSFileImpl(diskManager, vfsPath, dataBlockLocation);

		diskManager.addJournalItem(new CreateFileItem(vfsPath));
		return entry;
	}

	protected static VFSFileImpl createNewFile(VFSDiskManagerImpl diskManager, VFSPathImpl vfsPath) throws VFSException, IOException {
		DataBlock dataBlock = null;
		try {
			dataBlock = diskManager.getDataSectionHandler().allocateNewDataBlock(true);
			return createNewFile(diskManager, vfsPath, dataBlock.getLocation());
		} catch (IOException ex) {

			if (dataBlock != null) {
				diskManager.getDataSectionHandler().freeDataBlock(dataBlock);
			}

			throw ex;
		}
	}

	private static void insertEntryIntoParentFolder(VFSDiskManagerImpl diskManager, VFSPathImpl vfsPath, long dataBlockLocation, DirectoryBlock directoryBlock)
			throws VFSException, IOException {

		// get parent directory
		VFSPathImpl parentPath = new VFSPathImpl(diskManager, vfsPath.getParentPath());
		VFSDirectoryImpl parentDirectory = (VFSDirectoryImpl) parentPath.getVFSEntry();

		DirectoryEntryBlock directoryEntryBlock = new DirectoryEntryBlock(vfsPath.getName());

		directoryEntryBlock.assignDataBlockLocation(dataBlockLocation);

		if (directoryBlock != null) {
			// allocate DirectoryBlock which will contain subdirectories of that directory were about to create
			directoryEntryBlock.assignDirectoryBlock(directoryBlock);
		}

		parentDirectory.getChildTree().insert(diskManager.getDirectorySectionHandler(), directoryEntryBlock);
	}

	/**
	 * Reload first DataBlock from file
	 * 
	 * @return
	 * @throws VFSException
	 */
	protected DataBlock getFirstDataBlock() throws VFSException {
		return diskManager.getDataSectionHandler().loadDataBlock(firstDataBlockLocation);
	}

	public void setDataBlock(DataBlock dataBlock) {
		if (firstDataBlockLocation != -1) {
			// this should not happen
			throw new VFSRuntimeException("Internal error - Overriding DataBlock of " + this);
		}

		this.firstDataBlockLocation = dataBlock.getLocation();
	}

	@Override
	public abstract void copyTo(VFSPath newLocation) throws VFSException;

	@Override
	public abstract List<VFSEntry> getChildren() throws VFSException;

	/**
	 * Returns a single VFSEntry
	 * 
	 * 
	 * 
	 * does only work on directories
	 * 
	 * @param fileName
	 *            name of the file or folder contained by this VFSEntry
	 * @return
	 */
	public abstract VFSEntryImpl getChildByName(String fileName) throws VFSException;

	@Override
	public abstract InputStream getInputStream() throws VFSException;

	@Override
	public abstract OutputStream getOutputStream(int writeMode) throws VFSException;

	@Override
	public VFSPath getPath() {
		return path;
	}

	@Override
	public void moveTo(VFSPath newLocation) throws VFSException {
		if (newLocation.exists()) {
			throw new VFSException("Copy failed - file already exist " + newLocation.getAbsolutePath());
		}

		String oldPath = path.getAbsolutePath();
		String newPath = newLocation.getAbsolutePath();
		LOGGER.info("Move Entry " + oldPath + " to " + newPath);

		VFSDirectoryImpl oldParentDirectory = getParentProtected();

		String newName = newLocation.getName();

		String parentPathString = newLocation.getParentPath();
		VFSPath newParentPath = diskManager.createPath(parentPathString);

		VFSDirectoryImpl newParentDirectory;
		if (newParentPath.exists()) {
			newParentDirectory = (VFSDirectoryImpl) newParentPath.getVFSEntry();
		} else {
			newParentDirectory = (VFSDirectoryImpl) newParentPath.createDirectory();
		}

		String oldName = path.getName();

		// do the renaming
		oldParentDirectory.moveDirectoryEntry(oldName, newParentDirectory, newName);

		// assign new path
		this.path = (VFSPathImpl) newLocation;

		diskManager.addJournalItem(new MoveToItem(oldPath, newPath));
	}

	@Override
	public void renameTo(String newName) throws VFSException {
		try {
			String oldPath = getPath().getAbsolutePath();
			LOGGER.info("Rename " + oldPath + " to " + newName);
			String oldName = getPath().getName();
			VFSDirectoryImpl parent = getParentProtected();

			// validation
			DirectoryEntryBlock childDirBlock = parent.getChildDirectoryEntryBlockByName(newName);
			if (childDirBlock != null) {
				throw new VFSDuplicatedEntryException("Rename " + getPath().getAbsolutePath() + " to " + newName + " failed because name is already taken");
			}

			parent.renameDirectoryEntryBlock(oldName, newName);
			this.path = this.path.renameTo(newName);

			diskManager.addJournalItem(new RenameEntryItem(oldPath, newName));
		} catch (IOException ex) {
			throw new VFSException(ex);
		}
	}

	@Override
	public void delete() throws VFSException {
		try {
			VFSDirectoryImpl parentDirectory = (VFSDirectoryImpl) getParent();

			if (parentDirectory == this) {
				throw new VFSException("Don't try to delete the root directory fool");
			}

			diskManager.addJournalItem(new DeleteEntryItem(this));
			parentDirectory.deleteChild(this);
		} catch (IOException e) {
			throw new VFSException(e);
		}
	}

	@Override
	public abstract boolean isDirectory();

	@Override
	public VFSPath getChildPath(String childName) throws VFSException {

		String thisPath = getPath().getAbsolutePath();

		String childPath;
		if (VFSPath.FILE_SEPARATOR.equals(thisPath)) {
			// this is the Root Entry
			childPath = VFSPath.FILE_SEPARATOR + childName;
		} else {
			childPath = thisPath + VFSPath.FILE_SEPARATOR + childName;
		}

		VFSPathImpl childPathObj = new VFSPathImpl(diskManager, childPath);
		return childPathObj;
	}

	@Override
	public VFSEntry getParent() throws VFSException {
		return getParentProtected();
	}

	protected VFSDirectoryImpl getParentProtected() throws VFSException {
		String parentPathString = path.getParentPath();

		VFSPath parentPath = diskManager.createPath(parentPathString);

		if (!parentPath.exists()) {
			throw new VFSException("Internal Error while trying to get Parent of " + path.getAbsolutePath());
		}

		return (VFSDirectoryImpl) parentPath.getVFSEntry();
	}

	/**
	 * delete user data of this entry
	 * 
	 * @throws IOException
	 */
	protected void deleteDataBlocks() throws VFSException, IOException {
		DataBlock firstDataBlock = getFirstDataBlock();
		long next = firstDataBlock.getNextDataBlock();
		diskManager.getDataSectionHandler().freeDataBlock(firstDataBlock);

		freeSubsequentDataBlocks(next);
	}

	/**
	 * sets user data to 0 for this Entry
	 * 
	 * @throws VFSException
	 * @throws IOException
	 */
	protected void truncateDataBlocks() throws VFSException, IOException {
		DataBlock firstDataBlock = getFirstDataBlock();

		long next = firstDataBlock.getNextDataBlock();

		firstDataBlock.setDataLength(0);
		firstDataBlock.setNextDataBlock(0);
		diskManager.getDataSectionHandler().persistDataBlock(firstDataBlock);

		freeSubsequentDataBlocks(next);
	}

	private void freeSubsequentDataBlocks(long next) throws VFSException, IOException {
		while (next != 0) {
			DataBlock tmp = diskManager.getDataSectionHandler().loadDataBlock(next);
			next = tmp.getNextDataBlock();
			diskManager.getDataSectionHandler().freeDataBlock(tmp);
		}
	}
}
