package ch.eth.jcd.badgers.vfs.core.journaling;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.journaling.items.JournalItem;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class Journal implements Serializable {
	private static final Logger LOGGER = Logger.getLogger(Journal.class);

	private static final long serialVersionUID = -4125853629235672102L;

	private final List<JournalItem> journalEntries = new ArrayList<>();
	private final String journalFolderName;

	public Journal(List<JournalItem> uncommitedJournalEntries, String journalFolderName) {
		this.journalFolderName = journalFolderName;
		// copy entries
		if (LOGGER.isTraceEnabled()) {
			for (JournalItem journalItem : uncommitedJournalEntries) {
				LOGGER.trace("Adding " + journalItem + " to journal");
			}
		}
		journalEntries.addAll(uncommitedJournalEntries);
	}

	public void addJournalEntry(JournalItem entry) {
		LOGGER.trace("Adding " + entry + " to journal");
		journalEntries.add(entry);
	}

	public void replay(VFSDiskManager diskManager) throws VFSException {
		if (LOGGER.isTraceEnabled()) {
			for (JournalItem item : journalEntries) {
				LOGGER.trace("About to Replay: " + item);
			}
		}
		for (JournalItem entry : journalEntries) {
			entry.replay(diskManager);
		}
	}

	/**
	 * Revert modifications done
	 * 
	 * reverting changes does not affect Journals
	 * 
	 * @throws VFSException
	 */
	public void revert(VFSDiskManager diskManager) throws VFSException {
		// revert entries in opposite order
		for (int i = journalEntries.size() - 1; i >= 0; i--) {
			JournalItem entry = journalEntries.get(i);
			entry.revert(diskManager);
		}
	}

	public void replayResolveConflics(VFSDiskManager clientDiskManager, String conflictSuffix, List<PathConflict> conflicts) throws VFSException {

		for (int i = 0; i < journalEntries.size(); i++) {
			JournalItem entry = journalEntries.get(i);
			entry.replayResolveConflics(clientDiskManager, conflictSuffix, conflicts);
		}
	}

	public List<JournalItem> getJournalEntries() {
		return journalEntries;
	}

	public void beforeLocalTransport(VFSDiskManager diskManager) throws VFSException {
		for (JournalItem entry : journalEntries) {
			entry.beforeLocalTransport(diskManager);
		}
	}

	public void beforeRmiTransport(VFSDiskManager diskManager) throws VFSException {
		for (JournalItem entry : journalEntries) {
			entry.beforeRmiTransport(diskManager);
		}
	}

	public void afterRmiTransport(VFSDiskManager diskManager) {
		for (JournalItem entry : journalEntries) {
			entry.afterRmiTransport(diskManager);
		}
	}

	public String getJournalFolderName() {
		return journalFolderName;
	}

}
