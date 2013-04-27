package ch.eth.jcd.badgers.vfs.core.journaling;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.core.VFSPathImpl;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.items.JournalItem;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.util.ByteUtil;

public class VFSJournaling {

	private static final Logger LOGGER = Logger.getLogger(VFSJournaling.class);

	private static final String HIDDEN_FOLDER_NAME = ".hidden";

	private List<JournalItem> uncommitedJournalEntries = new ArrayList<>();

	private final VFSDiskManagerImpl diskManager;

	/**
	 * Flag disables journaling temporary
	 * 
	 * we dont need journaling while writing journal information to the disk
	 */
	private boolean journalingEnabled = true;

	public VFSJournaling(VFSDiskManagerImpl diskManager) {
		this.diskManager = diskManager;
	}

	public Journal closeAndGetCurrentJournal() throws VFSException {
		Journal j = new Journal(uncommitedJournalEntries);
		uncommitedJournalEntries.clear();
		return j;
	}

	public void closeJournal() throws VFSException {

		VFSEntry journalsFolder = getJournalsFolder();

		// determine name of the journal file

		List<VFSEntry> journals = journalsFolder.getChildren();

		long journalNumber = 1 + journals.size() + getLastSeenServerVersion();

		writeJournal(journalsFolder, journalNumber, uncommitedJournalEntries);
		uncommitedJournalEntries.clear();
	}

	private void writeJournal(VFSEntry journalsFolder, long journalNumber, List<JournalItem> uncommitedJournalEntries) throws VFSException {
		journalingEnabled = false;
		try {
			NumberFormat decimalFormat = new DecimalFormat("000000000000000");

			VFSPath journalPath = journalsFolder.getChildPath(decimalFormat.format(journalNumber));
			VFSEntry journalFile = journalPath.createFile();

			Journal toPersist = new Journal(uncommitedJournalEntries);
			try (OutputStream out = journalFile.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
				ObjectOutputStream objOout = new ObjectOutputStream(out);
				objOout.writeObject(toPersist);

			} catch (IOException e) {
				throw new VFSException(e);
			}
		} finally {
			journalingEnabled = true;
		}
	}

	private VFSEntry getJournalsFolder() throws VFSException {
		journalingEnabled = false;
		try {

			VFSPathImpl hiddenPath = (VFSPathImpl) diskManager.getRoot().getChildPath(HIDDEN_FOLDER_NAME);
			VFSEntry hiddenEntry;
			if (!hiddenPath.exists()) {
				hiddenEntry = hiddenPath.createDirectory();
			} else {
				hiddenEntry = hiddenPath.getVFSEntry();
			}

			VFSPath journalsPath = hiddenEntry.getChildPath("journals");

			if (journalsPath.exists()) {
				return journalsPath.getVFSEntry();
			} else {
				return journalsPath.createDirectory();
			}
		} finally {
			journalingEnabled = true;
		}
	}

	public void addJournalItem(JournalItem journalEntry) {
		if (journalingEnabled) {
			uncommitedJournalEntries.add(journalEntry);
		} else {
			LOGGER.debug("Journaling disabled drop " + journalEntry);
		}
	}

	private long getLastSeenServerVersion() throws VFSException {

		journalingEnabled = false;
		try {

			long serverVersion = 0;
			VFSPath syncedVersion = diskManager.getRoot().getChildPath(HIDDEN_FOLDER_NAME + VFSPath.FILE_SEPARATOR + "syncedversion.txt");
			VFSEntry syncedVersionFile;
			if (!syncedVersion.exists()) {
				syncedVersionFile = syncedVersion.createFile();

				try (OutputStream out = syncedVersionFile.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
					out.write(ByteUtil.longToBytes(serverVersion));
				} catch (IOException e) {
					throw new VFSException("", e);
				}

			} else {
				syncedVersionFile = syncedVersion.getVFSEntry();

				try (InputStream in = syncedVersionFile.getInputStream()) {
					byte[] longByteBuffer = new byte[8];

					if (8 != in.read(longByteBuffer)) {
						throw new VFSException("Could not read VersionNumber");
					}

					serverVersion = ByteUtil.bytesToLong(longByteBuffer);
				} catch (IOException e) {
					throw new VFSException("", e);
				}
			}

			return serverVersion;
		} finally {
			journalingEnabled = true;
		}
	}
}
