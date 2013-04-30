package ch.eth.jcd.badgers.vfs.core.journaling;

import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.journaling.items.JournalItem;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class VFSDisabledJournaling implements VFSJournaling {

	public VFSDisabledJournaling() {
	}

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
	public Journal createJournal(VFSEntry root) throws VFSException {
		return null;
	}

	@Override
	public void pauseJournaling(boolean pause) {
	}
}
