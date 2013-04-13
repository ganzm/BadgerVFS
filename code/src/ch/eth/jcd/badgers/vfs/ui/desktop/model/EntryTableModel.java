package ch.eth.jcd.badgers.vfs.ui.desktop.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class EntryTableModel implements TableModel {

	private final List<TableModelListener> listeners = new ArrayList<TableModelListener>();

	private final List<EntryUiModel> entries = new ArrayList<EntryUiModel>();

	public List<EntryUiModel> getEntries() {
		return entries;
	}

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
		if (rowIndex < entries.size()) {
			EntryUiModel entry = entries.get(rowIndex);
			if (columnIndex == 0) {
				return entry;
			}
		}

		return null;
	}

	public void updatedValueAt(int currentEditedRow, int currentEditedColumn) {
		TableModelEvent updateEvent = new TableModelEvent(this, currentEditedColumn, currentEditedColumn, TableModelEvent.UPDATE);
		for (TableModelListener listener : listeners) {
			listener.tableChanged(updateEvent);
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		Object entryModel = getValueAt(rowIndex, columnIndex);
		if (entryModel != null && entryModel instanceof ParentFolderEntryUiModel) {
			return false;
		}
		return true;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

		// strange code with strange behaviour of CellEditor - dont touch
		if (!(aValue instanceof EntryUiModel)) {
			return;
		}

		entries.remove(rowIndex);
		entries.add(rowIndex, (EntryUiModel) aValue);

		TableModelEvent updateEvent = new TableModelEvent(this, rowIndex, rowIndex, 0, TableModelEvent.UPDATE);
		for (TableModelListener listener : listeners) {
			listener.tableChanged(updateEvent);
		}

	}

	public void setEntries(ParentFolderEntryUiModel parentFolderEntryModel, List<EntryUiModel> newEntries) {
		int oldSize = entries.size();
		entries.clear();

		TableModelEvent removeEvent = new TableModelEvent(this, 0, oldSize, 0, TableModelEvent.DELETE);
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

	public void appendEntry(EntryUiModel entryModel) {
		entries.add(entryModel);

		int newSize = entries.size();

		TableModelEvent addedEvent = new TableModelEvent(this, newSize - 1, newSize, 0, TableModelEvent.INSERT);
		for (TableModelListener listener : listeners) {
			listener.tableChanged(addedEvent);
		}
	}
}
