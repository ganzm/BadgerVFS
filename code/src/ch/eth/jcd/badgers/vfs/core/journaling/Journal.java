package ch.eth.jcd.badgers.vfs.core.journaling;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.journaling.items.JournalItem;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.streaming.RemoteInputStream;
import ch.eth.jcd.badgers.vfs.remote.streaming.RemoteOutputStream;

public class Journal implements Serializable {

	private static final long serialVersionUID = -4125853629235672102L;

	/**
	 * This is the Version last seen on the synchronization server
	 * 
	 * up to this version there are no differences from the local to the server side version
	 * 
	 */
	private long serverVersion;

	/**
	 * Ignore this field if you are running code on the synchronization server
	 * 
	 */
	private long clientVersion;

	private List<JournalItem> journalEntries = new ArrayList<>();

	private Map<UUID, RemoteInputStream> entriesToUpload;
	private Map<UUID, RemoteOutputStream> entriesToDownload;

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
