package ch.eth.jcd.badgers.vfs.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
import ch.eth.jcd.badgers.vfs.core.data.DataSectionHandler;
import ch.eth.jcd.badgers.vfs.core.directory.DirectoryChildTree;
import ch.eth.jcd.badgers.vfs.core.directory.DirectoryEntryBlock;
import ch.eth.jcd.badgers.vfs.core.directory.DirectorySectionHandler;
import ch.eth.jcd.badgers.vfs.core.interfaces.FindInFolderCallback;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.items.ModifyFileItem;
import ch.eth.jcd.badgers.vfs.core.model.SearchParameter;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

/**
 * $Id$
 * 
 * This class represents a single file located on the virtual file system
 * 
 */
public class VFSFileImpl extends VFSEntryImpl {

	private static final Logger LOGGER = Logger.getLogger(VFSFileImpl.class);

	protected VFSFileImpl(VFSDiskManagerImpl diskManager, VFSPathImpl path, long firstDataBlockLocation) {
		super(diskManager, path, firstDataBlockLocation);
	}

	@Override
	public List<VFSEntry> getChildren() {
		LOGGER.debug("Tried to call getChildren on File " + getPath());
		return null;
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public VFSEntryImpl getChildByName(String fileName) {
		LOGGER.debug("Tried to call getChildByName on File " + getPath());
		return null;
	}

	@Override
	public String toString() {
		return "File " + path;
	}

	@Override
	public InputStream getInputStream() throws VFSException {
		VFSFileInputStream inputStream = new VFSFileInputStream(diskManager.getDataSectionHandler(), getFirstDataBlock());
		return diskManager.wrapInputStream(inputStream);
	}

	@Override
	public OutputStream getOutputStream(int writeMode) throws VFSException {

		try {
			DataBlock firstDataBlock = getFirstDataBlock();
			if (firstDataBlock.getLinkCount() <= 1) {
				// there is only one DirectoryEntryBlock pointing to this file content
				truncateDataBlocks();
			} else {
				// there is more than one DirectoryEntryBlock pointing to this file content
				String fileName = getPath().getName();

				DirectorySectionHandler directorySectionHandler = diskManager.getDirectorySectionHandler();
				DataSectionHandler dataSectionHandler = diskManager.getDataSectionHandler();

				// allocate new DataBlock and clone header
				DataBlock newBlock = dataSectionHandler.allocateNewDataBlock(true);
				newBlock.setCreationDate(firstDataBlock.getCreationDate());

				// assign the newly created DataBlock to the corresponding DirectoryEntry
				VFSDirectoryImpl parentDir = getParentProtected();
				DirectoryChildTree childTree = parentDir.getChildTree();
				DirectoryEntryBlock removedEntry = childTree.remove(directorySectionHandler, fileName);
				removedEntry.assignDataBlockLocation(newBlock.getLocation());
				childTree.insert(directorySectionHandler, removedEntry);

				// decrease DataBlock LinkCount
				firstDataBlock.decLinkCount();
				diskManager.getDataSectionHandler().persistDataBlock(firstDataBlock);
				diskManager.getDataSectionHandler().persistDataBlock(newBlock);

				firstDataBlockLocation = newBlock.getLocation();
				firstDataBlock = newBlock;

			}

			diskManager.addJournalItem(new ModifyFileItem(this));

			// reload DataBlock since addJournal copies this file to the journal section (yes, this is nasty)
			firstDataBlock = diskManager.getDataSectionHandler().loadDataBlock(firstDataBlock.getLocation());

			VFSFileOutputStream outputStream = new VFSFileOutputStream(diskManager.getDataSectionHandler(), firstDataBlock);
			return diskManager.wrapOutputStream(outputStream);

		} catch (IOException e) {
			throw new VFSException("Error while truncating file", e);
		}
	}

	@Override
	public void copyTo(VFSPath newLocation) throws VFSException {
		if (newLocation.exists()) {
			throw new VFSException("Copy failed - file already exist " + newLocation.getAbsolutePath());
		}

		DataBlock firstDataBlock = getFirstDataBlock();

		LOGGER.info("Copy file " + path.getAbsolutePath() + " to " + newLocation.getAbsolutePath());
		if (firstDataBlock.getLinkCount() < DataBlock.MAX_LINK_COUNT) {
			shallowCopy(firstDataBlock, newLocation);
		} else {
			deepCopy(firstDataBlock, newLocation);
		}
	}

	private void deepCopy(DataBlock firstDataBlock, VFSPath newLocation) throws VFSException {
		OutputStream out = null;
		InputStream in = null;

		try {
			// copy as usual
			VFSFileImpl newFile = (VFSFileImpl) newLocation.createFile();

			// since we do an file system internal copy we bypass the getOutputStream/getInputStream method to avoid compression/decompression
			// encryption/decryption
			// get outputstream of new file
			out = new VFSFileOutputStream(diskManager.getDataSectionHandler(), newFile.getFirstDataBlock());
			// get input stream of this file
			in = new VFSFileInputStream(diskManager.getDataSectionHandler(), firstDataBlock);

			byte[] buffer = new byte[DataBlock.BLOCK_SIZE];

			int numBytes;
			while ((numBytes = in.read(buffer)) >= 0) {
				out.write(buffer, 0, numBytes);
			}

			diskManager.addJournalItem(new ModifyFileItem(newFile));
		} catch (IOException ex) {
			throw new VFSException("Error while copying data from " + path.getAbsolutePath() + " to " + newLocation.getAbsolutePath(), ex);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					LOGGER.error("", e);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOGGER.error("", e);
				}
			}
		}
	}

	private void shallowCopy(DataBlock firstDataBlock, VFSPath newLocation) throws VFSException {
		try {
			LOGGER.debug("Do shallow copy operation from " + getPath().getAbsolutePath() + " to " + newLocation.getAbsolutePath());
			firstDataBlock.incLinkCount();
			diskManager.getDataSectionHandler().persistDataBlock(firstDataBlock);
			VFSFileImpl newFile = VFSEntryImpl.createNewFile(diskManager, (VFSPathImpl) newLocation, firstDataBlock.getLocation());

			diskManager.addJournalItem(new ModifyFileItem(newFile));
		} catch (IOException e) {
			throw new VFSException(e);
		}
	}

	@Override
	public void findInFolder(String fileName, FindInFolderCallback observer) throws VFSException {
		throw new VFSException("find operation not supported for files");
	}

	@Override
	public void findInFolder(SearchParameter searchParameter, FindInFolderCallback observer) throws VFSException {
		throw new VFSException("find operation not supported for files");
	}
}
