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
	@Override
	public void closeJournal() throws VFSException {
		if (uncommitedJournalEntries == null) {
			currentJournalFolder = null;
			// nothing to do
			return;
		}

		writeJournal(currentJournalFolder, uncommitedJournalEntries);
		uncommitedJournalEntries = null;
		currentJournalFolder = null;
	}

	@Override
	public List<Journal> getPendingJournals() throws VFSException {
		List<Journal> journals = new ArrayList<>();
		VFSEntry journalsFolder = getJournalsFolder();

		List<VFSEntry> journalEntries = journalsFolder.getChildren();
		for (VFSEntry journalFolder : journalEntries) {
			Journal journal = deserializeJournalFromFolder(journalFolder);
			journals.add(journal);
		}
		return journals;
	}

	private Journal deserializeJournalFromFolder(VFSEntry journalFolder) throws VFSException {
		assert journalFolder.isDirectory();

		VFSPath journalFilePath = journalFolder.getChildPath(JOURNAL_NAME);
		if (!journalFilePath.exists()) {
			throw new VFSException("Journal file " + JOURNAL_NAME + " not found in " + journalFolder.getPath().getAbsolutePath());
		}

		VFSEntry journalFile = journalFilePath.getVFSEntry();

		Journal journal = deserializeJournalEntry(journalFile);
		return journal;
	}

	private Journal deserializeJournalEntry(VFSEntry journalEntry) throws VFSException {
		try (InputStream in = journalEntry.getInputStream()) {
			try (ObjectInput objIn = new ObjectInputStream(in)) {
				Journal journal = (Journal) objIn.readObject();
				return journal;
			}
		} catch (IOException | ClassNotFoundException e) {
			throw new VFSException(e);
		}
	}

	private void writeJournal(VFSEntry journalFolder, List<JournalItem> uncommitedJournalEntries) throws VFSException {

		boolean journalingEnabledBackupFlag = journalingEnabled;
		journalingEnabled = false;
		try {
			VFSPath journalPath = journalFolder.getChildPath(JOURNAL_NAME);
			VFSEntry journalFile = journalPath.createFile();

			Journal toPersist = new Journal(uncommitedJournalEntries);
			try (OutputStream out = journalFile.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
				ObjectOutputStream objOout = new ObjectOutputStream(out);
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

			hiddenEntry = hiddenPath.exists() ? hiddenPath.getVFSEntry() : hiddenPath.createDirectory();

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

	@Override
	public void addJournalItem(JournalItem journalEntry) throws VFSException {
		if (journalingEnabled) {
			if (uncommitedJournalEntries == null) {
				openNewJournal(false);
			}
			uncommitedJournalEntries.add(journalEntry);
			journalEntry.onJournalAdd(this);
		} else {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Journaling disabled -  drop " + journalEntry);
			}
		}
	}

	@Override
	public void openNewJournal(boolean doEnableJournaling) throws VFSException {
		openNewJournal(new ArrayList<JournalItem>(0), doEnableJournaling);
	}

	@Override
	public void openNewJournal(List<JournalItem> journalItemsToAdd, boolean doEnableJournaling) throws VFSException {
		boolean journalingEnabledBackupFlag = journalingEnabled;
		journalingEnabled = doEnableJournaling;
		try {

			VFSEntry journalsFolder = getJournalsFolder();

			List<VFSEntry> journals = journalsFolder.getChildren();

			long newJournalNumber = 0;
			if (journals.isEmpty()) {
				newJournalNumber = 1 + diskManager.getServerVersion();
			}

			else {
				String journalName = journals.get(journals.size() - 1).getPath().getName();
				newJournalNumber = 1 + Long.parseLong(journalName);
			}

			String newJournalName = versionToJournalFolderName(newJournalNumber);

			VFSPath newJournalPath = journalsFolder.getChildPath(newJournalName);

			LOGGER.info("open new Journal on " + newJournalPath.getAbsolutePath());
			currentJournalFolder = newJournalPath.createDirectory();
			uncommitedJournalEntries = new ArrayList<JournalItem>(journalItemsToAdd);
		} finally {
			journalingEnabled = journalingEnabledBackupFlag;
		}
	}

	private String versionToJournalFolderName(long versionNumber) {
		NumberFormat decimalFormat = new DecimalFormat("000000000000000");
		String newJournalName = decimalFormat.format(versionNumber);
		return newJournalName;
	}

	/**
	 * scans the whole disk and creates a journals which when replayed creates exactly the same content
	 * 
	 * but we don't want to persist the journal on the client file system
	 * 
	 * @param root
	 * @return
	 * @throws VFSException
	 */
	@Override
	public Journal journalizeDisk(VFSEntry root) throws VFSException {

		if (uncommitedJournalEntries != null) {
			throw new VFSException("Disallowed action - use this method only for unlinked/unjournalized disks");
		}

		uncommitedJournalEntries = new ArrayList<>();
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

				ModifyFileItem modifyFile = new ModifyFileItem((VFSFileImpl) childEntry);
				modifyFile.suppressOnJournalAddJournalCopy();
				addJournalItem(modifyFile);
			}
		}
	}

	@Override
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

	@Override
	public List<Journal> getJournalsSince(long lastSeenServerVersion) throws VFSException {
		List<Journal> result = new ArrayList<>();

		String lastSeenJournal = versionToJournalFolderName(lastSeenServerVersion);
		VFSEntry journalsFolder = getJournalsFolder();
		List<VFSEntry> journalFolders = journalsFolder.getChildren();

		for (VFSEntry journalFolder : journalFolders) {
			String journalFolderName = journalFolder.getPath().getName();
			if (journalFolderName.compareTo(lastSeenJournal) > 0) {
				Journal journal = deserializeJournalFromFolder(journalFolder);
				result.add(journal);
			}
		}

		return result;
	}

	@Override
	public void deleteJournals() throws VFSException {
		// expect this method to be called after having synchronized data to the SynchronisationServer
		assert currentJournalFolder == null;

		boolean journalingEnabledBackupFlag = journalingEnabled;
		journalingEnabled = false;
		try {
			VFSEntry journalsFolder = getJournalsFolder();
			for (VFSEntry journal : journalsFolder.getChildren()) {
				LOGGER.debug("Delete old Journal " + journal.getPath().getAbsolutePath());
				journal.delete();
			}
		} finally {
			journalingEnabled = journalingEnabledBackupFlag;
		}
	}
}
