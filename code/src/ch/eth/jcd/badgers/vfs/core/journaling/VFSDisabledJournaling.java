package ch.eth.jcd.badgers.vfs.core.journaling;

import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.items.JournalItem;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class VFSDisabledJournaling implements VFSJournaling {

	@Override
	public void addJournalItem(JournalItem journalEntry) {
	}

	@Override
	public void closeJournal() throws VFSException {
	}

	@Override
	public List<Journal> getPendingJournals() throws VFSException {
		return null;
	}

	@Override
	public Journal journalizeDisk(VFSEntry root) throws VFSException {
		return null;
	}

	@Override
	public void pauseJournaling(boolean pause) {
	}

	@Override
	public VFSPath copyFileToJournal(String absolutePath) throws VFSException {
		return null;
	}

	@Override
	public void persistServerJournal(Journal journal) {

	}

	@Override
	public void openNewJournal(boolean doEnableJournaling) throws VFSException {
	}

	@Override
	public void openNewJournal(List<JournalItem> journalItemsToAdd, boolean doEnableJournaling) throws VFSException {
	}

	@Override
	public List<Journal> getJournalsSince(long lastSeenServerVersion) {
		return null;
	}

	@Override
	public void deleteJournals() throws VFSException {

	}
}
