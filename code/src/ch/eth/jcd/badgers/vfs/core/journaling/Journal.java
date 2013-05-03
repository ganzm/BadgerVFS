package ch.eth.jcd.badgers.vfs.core.journaling;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.journaling.items.JournalItem;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class Journal implements Serializable {

	private static final long serialVersionUID = -4125853629235672102L;

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

	public void beforeSerializeToDisk() throws VFSException {
		for (JournalItem entry : journalEntries) {
			entry.beforeSerializeToDisk();
		}
	}

	public void afterDeserializeFromDisk(VFSDiskManager diskManager) throws VFSException {
		for (JournalItem entry : journalEntries) {
			entry.afterDeserializeFromDisk(diskManager);
		}
	}

}
