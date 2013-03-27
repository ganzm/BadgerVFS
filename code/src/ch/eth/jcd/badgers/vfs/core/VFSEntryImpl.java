package ch.eth.jcd.badgers.vfs.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
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
	public VFSEntryImpl(VFSDiskManagerImpl diskManager, VFSPath path) {
		this.diskManager = diskManager;
		this.path = path;
	}

	/**
	 * TODO move to me a factory if you want to
	 * 
	 * @param vfsPathImpl
	 * @return
	 */
	protected static VFSEntryImpl createNewDirectory(VFSDiskManagerImpl diskManager, VFSPathImpl vfsPathImpl) {
		throw new UnsupportedOperationException("TODO");

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
	public abstract List<VFSEntry> getChildren();

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
	public abstract VFSEntry getChildByName(String fileName) throws VFSException;

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
	public boolean isDirectory() {
		throw new UnsupportedOperationException("TODO");
	}

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
