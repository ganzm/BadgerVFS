package ch.eth.jcd.badgers.vfs.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
import ch.eth.jcd.badgers.vfs.core.directory.DirectoryBlock;
import ch.eth.jcd.badgers.vfs.core.directory.DirectoryEntryBlock;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;

public abstract class VFSEntryImpl implements VFSEntry {

	private final VFSPath path;
	protected final VFSDiskManagerImpl diskManager;

	private DataBlock firstDataBlock;

	/**
	 * creates a new
	 * 
	 * @param path
	 */
	public VFSEntryImpl(VFSDiskManagerImpl diskManager, VFSPath path, DataBlock firstDataBlock) {
		this.diskManager = diskManager;
		this.path = path;
		this.firstDataBlock = firstDataBlock;
	}

	/**
	 * TODO move to me a factory if you want to
	 * 
	 * @param vfsPath
	 * @return
	 * @throws VFSException
	 * @throws IOException
	 */
	protected static VFSEntryImpl createNewDirectory(VFSDiskManagerImpl diskManager, VFSPathImpl vfsPath) throws VFSException, IOException {

		DataBlock dataBlock = null;
		DirectoryBlock directoryBlock = null;
		try {
			dataBlock = diskManager.getDataSectionHandler().allocateNewDataBlock(true);
			directoryBlock = diskManager.getDirectorySectionHandler().allocateNewDirectoryBlock();

			// get parent directory
			VFSPathImpl parentPath = new VFSPathImpl(diskManager, vfsPath.getParentPath());
			VFSDirectoryImpl parentDirectory = (VFSDirectoryImpl) parentPath.getVFSEntry();

			DirectoryEntryBlock directoryEntryBlock = new DirectoryEntryBlock(vfsPath.getName());

			directoryEntryBlock.assignDataBlock(dataBlock);

			// allocate DirectoryBlock which will contain subdirectories of that directory were about to create
			directoryEntryBlock.assignDirectoryBlock(directoryBlock);

			parentDirectory.getChildTree().insert(diskManager.getDirectorySectionHandler(), directoryEntryBlock);

			VFSDirectoryImpl entry = new VFSDirectoryImpl(diskManager, vfsPath, dataBlock, directoryBlock);

			return entry;
		} catch (Exception ex) {

			if (dataBlock != null) {
				diskManager.getDataSectionHandler().freeDataBlock(dataBlock);
			}

			if (directoryBlock != null) {
				diskManager.getDirectorySectionHandler().freeDirectoryBlock(directoryBlock);
			}

			throw ex;
		}
	}

	public void setDataBlock(DataBlock dataBlock) {
		if (firstDataBlock != null) {
			// this should not happen
			throw new VFSRuntimeException("Internal error - Overriding DataBlock of " + this);
		}

		this.firstDataBlock = dataBlock;
	}

	@Override
	public void copyTo(VFSPath newLocation) {
		throw new UnsupportedOperationException("TODO");
	}

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
	public InputStream getInputStream() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public OutputStream getOutputStream(int writeMode) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public VFSPath getPath() {
		return path;
	}

	@Override
	public void moveTo(VFSPath path) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void renameTo(String newName) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void delete() {
		throw new UnsupportedOperationException("TODO");
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
	public VFSEntry getParent() {
		throw new UnsupportedOperationException("TODO");
	}
}
