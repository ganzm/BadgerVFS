package ch.eth.jcd.badgers.vfs.core.journaling;

import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.journaling.items.JournalItem;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public interface VFSJournaling {

	void addJournalItem(JournalItem journalEntry);

	void closeJournal() throws VFSException;

	List<Journal> getPendingJournals() throws VFSException;

	Journal createJournal(VFSEntry root) throws VFSException;

	void pauseJournaling(boolean pause);
}
