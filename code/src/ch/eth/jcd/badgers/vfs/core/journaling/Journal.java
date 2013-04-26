package ch.eth.jcd.badgers.vfs.core.journaling;

import java.util.ArrayList;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.journaling.items.JournalItem;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class Journal {

	/**
	 * This is the Version last seen on the synchronisation server
	 * 
	 * up to this version there are no differences from the local to the server side version
	 * 
	 */
	private int serverVersion;

	/**
	 * Ignore this field if you are running code on the synchronisation server
	 * 
	 */
	private int clientVersion;

	private List<JournalItem> journalEntries = new ArrayList<>();

	public Journal(List<JournalItem> uncommitedJournalEntries) {
		// copy entries
		journalEntries.addAll(uncommitedJournalEntries);
	}

	public void addJournalEntry(JournalItem entry) {
		journalEntries.add(entry);
	}

	public void replay(VFSDiskManager diskManager) throws VFSException {
		for (JournalItem entry : journalEntries) {
			entry.replay(diskManager);
		}
	}

	public List<JournalItem> getJournalEntries() {
		return journalEntries;
	}
}
