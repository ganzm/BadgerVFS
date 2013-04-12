package ch.eth.jcd.badgers.vfs.ui.desktop.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class EntryUiListModel implements ListModel<EntryUiModel> {

	private List<ListDataListener> listeners = new ArrayList<ListDataListener>();

	private List<EntryUiModel> entries = new ArrayList<>();

	@Override
	public void addListDataListener(ListDataListener l) {
		listeners.add(l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
	}

	@Override
	public EntryUiModel getElementAt(int index) {
		return entries.get(index);
	}

	@Override
	public int getSize() {
		return entries.size();
	}

	public void setEntries(List<EntryUiModel> newEntries) {

		int oldSize = entries.size();

		entries.clear();

		ListDataEvent removeEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, oldSize);
		for (ListDataListener listener : listeners) {
			listener.intervalRemoved(removeEvent);
		}

		for (EntryUiModel newEntry : newEntries) {
			entries.add(newEntry);
		}

		int newSize = entries.size();

		ListDataEvent addedEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0, newSize);
		for (ListDataListener listener : listeners) {
			listener.intervalRemoved(addedEvent);
		}
	}
}
