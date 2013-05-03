package ch.eth.jcd.badgers.vfs.core.journaling;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.core.VFSFileImpl;
import ch.eth.jcd.badgers.vfs.core.VFSPathImpl;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.items.CreateDirectoryItem;
import ch.eth.jcd.badgers.vfs.core.journaling.items.CreateFileItem;
import ch.eth.jcd.badgers.vfs.core.journaling.items.JournalItem;
import ch.eth.jcd.badgers.vfs.core.journaling.items.ModifyFileItem;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

/**
 * $ID$
 * 
 * In order to synchronize data via SynchronisationServer the file system writes serializable journal files
 * 
 * 
 */
public class VFSJournalingImpl implements VFSJournaling {

	private static final Logger LOGGER = Logger.getLogger(VFSJournaling.class);

	public static final String HIDDEN_FOLDER_NAME = ".hidden";

	private static final String JOURNALS_FOLDER_NAME = "journals";

	private static final String JOURNAL_NAME = "journal.bin";

	/**
	 * Journal entries which not yet have been persisted
	 */
	private List<JournalItem> uncommitedJournalEntries = null;

	private VFSEntry currentJournalFolder;

	private final VFSDiskManagerImpl diskManager;

	/**
	 * Flag disables journaling temporary
	 * 
	 * we don't need journaling while writing journal information to the disk
	 */
	private boolean journalingEnabled = true;

	public VFSJournalingImpl(VFSDiskManagerImpl diskManager) {
		this.diskManager = diskManager;
	}

	/**
	 * Persist the current journal
	 */
	public void closeJournal() throws VFSException {
		if (uncommitedJournalEntries == null) {
			// nothing to do
			return;
		}

		writeJournal(currentJournalFolder, uncommitedJournalEntries);
		uncommitedJournalEntries = null;
	}

	public List<Journal> getPendingJournals() throws VFSException {
		List<Journal> journals = new ArrayList<>();
		VFSEntry journalsFolder = getJournalsFolder();

		List<VFSEntry> journalEntries = journalsFolder.getChildren();
		for (VFSEntry journalFolders : journalEntries) {
			assert journalsFolder.isDirectory();

			VFSPath journalFilePath = journalFolders.getChildPath(JOURNAL_NAME);
			if (!journalFilePath.exists()) {
				continue;
			}

			VFSEntry journalFile = journalFilePath.getVFSEntry();

			Journal journal = deserializeJournalEntry(journalFile);
			journals.add(journal);
		}
		return journals;
	}

	private Journal deserializeJournalEntry(VFSEntry journalEntry) throws VFSException {
		try (InputStream in = journalEntry.getInputStream()) {
			try (ObjectInput objIn = new ObjectInputStream(in)) {
				Journal journal = (Journal) objIn.readObject();
				journal.afterDeserializeFromDisk(diskManager);
				return journal;
			}
		} catch (IOException | ClassNotFoundException e) {
			throw new VFSException(e);
		}
	}

	private void writeJournal(VFSEntry journalsFolder, List<JournalItem> uncommitedJournalEntries) throws VFSException {

		boolean journalingEnabledBackupFlag = journalingEnabled;
		journalingEnabled = false;
		try {
			VFSPath journalPath = journalsFolder.getChildPath(JOURNAL_NAME);
			VFSEntry journalFile = journalPath.createFile();

			Journal toPersist = new Journal(uncommitedJournalEntries);
			try (OutputStream out = journalFile.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
				ObjectOutputStream objOout = new ObjectOutputStream(out);
				toPersist.beforeSerializeToDisk();
				objOout.writeObject(toPersist);

			} catch (IOException e) {
				throw new VFSException(e);
			}
		} finally {
			journalingEnabled = journalingEnabledBackupFlag;
		}
	}

	private VFSEntry getJournalsFolder() throws VFSException {
		boolean journalingEnabledBackupFlag = journalingEnabled;
		journalingEnabled = false;
		try {

			VFSPathImpl hiddenPath = (VFSPathImpl) diskManager.getRoot().getChildPath(HIDDEN_FOLDER_NAME);
			VFSEntry hiddenEntry;
			if (!hiddenPath.exists()) {
				hiddenEntry = hiddenPath.createDirectory();
			} else {
				hiddenEntry = hiddenPath.getVFSEntry();
			}

			VFSPath journalsPath = hiddenEntry.getChildPath(JOURNALS_FOLDER_NAME);

			if (journalsPath.exists()) {
				return journalsPath.getVFSEntry();
			} else {
				return journalsPath.createDirectory();
			}
		} finally {
			journalingEnabled = journalingEnabledBackupFlag;
		}
	}

	public void addJournalItem(JournalItem journalEntry) throws VFSException {
		if (journalingEnabled) {
			if (uncommitedJournalEntries == null) {

				boolean journalingEnabledBackupFlag = journalingEnabled;
				journalingEnabled = false;
				try {
					openNewJournal();
				} finally {
					journalingEnabled = journalingEnabledBackupFlag;
				}
			}
			uncommitedJournalEntries.add(journalEntry);
			journalEntry.onJournalAdd(this);
		} else {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Journaling disabled -  drop " + journalEntry);
			}
		}
	}

	public void openNewJournal() throws VFSException {
		openNewJournal(new ArrayList<JournalItem>(0));
	}

	public void openNewJournal(List<JournalItem> journalItemsToAdd) throws VFSException {

		VFSEntry journalsFolder = getJournalsFolder();

		List<VFSEntry> journals = journalsFolder.getChildren();

		long newJournalNumber = 0;
		if (journals.size() == 0) {
			newJournalNumber = 1 + diskManager.getServerVersion();
		}

		else {
			String journalName = journals.get(journals.size() - 1).getPath().getName();
			newJournalNumber = 1 + Long.parseLong(journalName);
		}

		NumberFormat decimalFormat = new DecimalFormat("000000000000000");
		String newJournalName = decimalFormat.format(newJournalNumber);

		VFSPath newJournalPath = journalsFolder.getChildPath(newJournalName);

		LOGGER.info("open new Journal on " + newJournalPath.getAbsolutePath());
		currentJournalFolder = newJournalPath.createDirectory();
		uncommitedJournalEntries = new ArrayList<JournalItem>(journalItemsToAdd);
	}

	/**
	 * scans the whole disk and creates a journals which when replayed creates exactly the same content
	 * 
	 * @param root
	 * @return
	 * @throws VFSException
	 */
	public Journal journalizeDisk(VFSEntry root) throws VFSException {

		if (uncommitedJournalEntries != null) {
			throw new VFSException("Disallowed action - use this method only for unlinked/unjournalized disks");
		}
		openNewJournal();

		addDirectoryToJournal(root);
		Journal j = new Journal(uncommitedJournalEntries);
		uncommitedJournalEntries = null;
		return j;
	}

	private void addDirectoryToJournal(VFSEntry entry) throws VFSException {
		for (VFSEntry childEntry : entry.getChildren()) {
			if (childEntry.isDirectory()) {
				addJournalItem(new CreateDirectoryItem(childEntry));
				addDirectoryToJournal(childEntry);
			} else {
				addJournalItem(new CreateFileItem(childEntry));
				addJournalItem(new ModifyFileItem((VFSFileImpl) childEntry));
			}
		}
	}

	public void pauseJournaling(boolean pause) {
		journalingEnabled = !pause;
	}

	@Override
	public VFSPath copyFileToJournal(String absolutePath) throws VFSException {
		boolean journalingEnabledBackupFlag = journalingEnabled;
		journalingEnabled = false;
		try {
			String fileName = "file-" + UUID.randomUUID();
			VFSPath targetPath = currentJournalFolder.getChildPath(fileName);

			VFSPath path = diskManager.createPath(absolutePath);
			VFSEntry entry = path.getVFSEntry();
			entry.copyTo(targetPath);

			return targetPath;
		} finally {
			journalingEnabled = journalingEnabledBackupFlag;
		}
	}

	@Override
	public void persistServerJournal(Journal journal) throws VFSException {
		uncommitedJournalEntries.addAll(journal.getJournalEntries());
		closeJournal();
	}
}
