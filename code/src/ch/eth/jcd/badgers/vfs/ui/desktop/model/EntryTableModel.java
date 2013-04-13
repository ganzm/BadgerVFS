package ch.eth.jcd.badgers.vfs.ui.desktop.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class EntryTableModel implements TableModel {

	private final List<TableModelListener> listeners = new ArrayList<TableModelListener>();

	private final List<EntryUiModel> entries = new ArrayList<EntryUiModel>();

	@Override
	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return EntryUiModel.class;
		}

		return null;
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return null;
	}

	@Override
	public int getRowCount() {
		return entries.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		EntryUiModel entry = entries.get(rowIndex);
		if (columnIndex == 0) {
			return entry;
		}

		return null;

	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub

	}

	public void setEntries(ParentFolderEntryUiModel parentFolderEntryModel, List<EntryUiModel> newEntries) {
		int oldSize = entries.size();
		entries.clear();

		// DataEvent removeEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, oldSize);
		TableModelEvent removeEvent = new TableModelEvent(this, 0, oldSize, TableModelEvent.DELETE);
		for (TableModelListener listener : listeners) {
			listener.tableChanged(removeEvent);
		}

		if (parentFolderEntryModel != null) {
			entries.add(parentFolderEntryModel);
		}

		for (EntryUiModel newEntry : newEntries) {
			entries.add(newEntry);
		}

		int newSize = entries.size();

		TableModelEvent addedEvent = new TableModelEvent(this, 0, newSize, 0, TableModelEvent.INSERT);
		for (TableModelListener listener : listeners) {
			listener.tableChanged(addedEvent);
		}
	}
}
