package ch.eth.jcd.badgers.vfs.core.journaling;

import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.items.JournalItem;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public interface VFSJournaling {

	void addJournalItem(JournalItem journalEntry) throws VFSException;

	void closeJournal() throws VFSException;

	List<Journal> getPendingJournals() throws VFSException;

	/**
	 * scans the whole disk and creates a journals which when replayed creates exactly the same content
	 * 
	 * @param root
	 * @return
	 * @throws VFSException
	 */
	Journal journalizeDisk(VFSEntry root) throws VFSException;

	void pauseJournaling(boolean pause);

	VFSPath copyFileToJournal(String absolutePath) throws VFSException;
}
