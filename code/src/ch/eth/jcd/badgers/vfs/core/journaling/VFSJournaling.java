package ch.eth.jcd.badgers.vfs.core.journaling;

import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.items.JournalItem;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public interface VFSJournaling {

	void addJournalItem(JournalItem journalEntry) throws VFSException;

	/**
	 * Persist the current journal
	 */
	void closeJournal() throws VFSException;

	/**
	 * gets a list of journals which have not yet been published to the synchronization server
	 * 
	 * @return
	 * @throws VFSException
	 */
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

	void persistServerJournal(Journal journal) throws VFSException;

	void openNewJournal() throws VFSException;

	void openNewJournal(List<JournalItem> journalItemsToAdd) throws VFSException;

	/**
	 * get all persisted journals from lastSeenServerVersion (excluded) up to the most recent one
	 * 
	 * @param lastSeenServerVersion
	 * @return any journal which is newer than lastSeenServerVersion
	 * @throws VFSException
	 */
	List<Journal> getJournalsSince(long lastSeenServerVersion) throws VFSException;
}
