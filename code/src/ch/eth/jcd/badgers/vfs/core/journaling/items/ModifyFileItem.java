package ch.eth.jcd.badgers.vfs.core.journaling.items;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.VFSFileImpl;
import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.PathConflict;
import ch.eth.jcd.badgers.vfs.core.journaling.VFSJournaling;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.streaming.RemoteInputStreamServer;

/**
 * $ID$
 * 
 * 
 * This JournalItem remembers when the content of a file has changed. Whenever this happens the file is (shallow) copied to a hidden section of the disk.
 * 
 * 
 * Why do we need this? Renaming files causes some trouble with the Journal. Since a journals needs to be serializable we are only allowed to save strings.
 * 
 * To avoid such problem we copy each file modification to a hidden section near the corresponding journal
 * 
 */
public class ModifyFileItem extends JournalItem {

	private static final long serialVersionUID = -1463585225400507478L;

	protected static final Logger LOGGER = Logger.getLogger(ModifyFileItem.class);

	/**
	 * Path where our data should be stored but may be touched/deleted/renamed by the user
	 */
	private final String absoluteFilePath;

	/**
	 * Path where our data are stored and where the user can not touch/rename/delete them
	 * 
	 * @param vfsFileImpl
	 * @throws VFSException
	 */
	private String journalPathString;

	private transient boolean suppressOnJournalAddJournalCopy = false;

	private InputStream inputStream;

	public ModifyFileItem(VFSFileImpl vfsFileImpl) throws VFSException {
		this.absoluteFilePath = vfsFileImpl.getPath().getAbsolutePath();
	}

	@Override
	public void onJournalAdd(VFSJournaling journaling) throws VFSException {
		if (!suppressOnJournalAddJournalCopy) {
			VFSPath journalPath = journaling.copyFileToJournal(absoluteFilePath);
			journalPathString = journalPath.getAbsolutePath();
		} else if (journalPathString == null) {
			journalPathString = absoluteFilePath;
		}
	}

	/**
	 * prevent that we copy the file content to the journal
	 */
	public void setSuppressOnJournalAddJournalCopy() {
		suppressOnJournalAddJournalCopy = true;
	}

	@Override
	public void beforeRmiTransport(VFSDiskManager diskManager) throws VFSException {
		try {
			VFSPath journalPath = diskManager.createPath(journalPathString);
			VFSEntry journalEntry = journalPath.getVFSEntry();
			this.inputStream = RemoteInputStreamServer.wrap(journalEntry.getInputStream());
		} catch (RemoteException ex) {
			throw new VFSException(ex);
		}
	}

	@Override
	public void afterRmiTransport(VFSDiskManager diskManager) {
		if (this.inputStream != null) {
			try {
				this.inputStream.close();
			} catch (IOException e) {
				LOGGER.error("Error while closing InputStream " + inputStream, e);
			}
			this.inputStream = null;
		}
	}

	@Override
	public void beforeLocalTransport(VFSDiskManager diskManager) throws VFSException {
		VFSPath journalPath = diskManager.createPath(journalPathString);
		VFSEntry journalEntry = journalPath.getVFSEntry();
		this.inputStream = journalEntry.getInputStream();
	}

	@Override
	public void doReplay(VFSDiskManager diskManager) throws VFSException {
		LOGGER.debug("Journal - Modify File " + absoluteFilePath);
		VFSPath filePath = diskManager.createPath(absoluteFilePath);
		VFSEntry file = filePath.getVFSEntry();

		// copy file
		try (OutputStream out = file.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
			byte[] buffer = new byte[DataBlock.BLOCK_SIZE];
			int numBytes;
			while ((numBytes = inputStream.read(buffer)) > 0) {
				out.write(buffer, 0, numBytes);
			}

			out.flush();

			// copy procedure finished

			boolean replayOnSyncServer = diskManager.getDiskConfiguration().isSyncServerMode();
			if (replayOnSyncServer) {
				VFSJournaling journaling = diskManager.getJournaling();
				VFSPath fileInJournalPath = journaling.copyFileToJournal(absoluteFilePath);
				this.journalPathString = fileInJournalPath.getAbsolutePath();
				suppressOnJournalAddJournalCopy = true;
			}
		} catch (IOException e) {
			throw new VFSException(e);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				LOGGER.error("Error while closing inputstream", e);
			}
			inputStream = null;
		}
	}

	@Override
	public String toString() {
		return "ModifyFileItem [absoluteFilePath=" + absoluteFilePath + ", journalPathString=" + journalPathString + ", suppressOnJournalAddJournalCopy="
				+ suppressOnJournalAddJournalCopy + "]";
	}

	@Override
	public void doRevert(VFSDiskManager diskManager) throws VFSException {

	}

	@Override
	public void doReplayResolveConflics(VFSDiskManager diskManager, String conflictSuffix, List<PathConflict> conflicts) throws VFSException {
		beforeLocalTransport(diskManager);
		doReplay(diskManager);
	}

	public String getAbsoluteFilePath() {
		return absoluteFilePath;
	}

	public String getJournalPathString() {
		return journalPathString;
	}
}
