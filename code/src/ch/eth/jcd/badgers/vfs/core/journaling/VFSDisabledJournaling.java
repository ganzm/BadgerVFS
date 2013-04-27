package ch.eth.jcd.badgers.vfs.core.journaling;

import ch.eth.jcd.badgers.vfs.core.journaling.items.JournalItem;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class VFSDisabledJournaling extends VFSJournaling {

	public VFSDisabledJournaling() {
		super(null);
	}

	@Override
	public void addJournalItem(JournalItem journalEntry) {
		// ignore this call since journaling is disabled
	}

	@Override
	public void closeJournal() throws VFSException {
		// ignore this call since journaling is disabled
	}
}
