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
	public void addTableModelListener(final TableModelListener l) {
		listeners.add(l);
	}

	@Override
	public void removeTableModelListener(final TableModelListener l) {
		listeners.remove(l);
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex) {
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
	public String getColumnName(final int columnIndex) {
		return null;
	}

	@Override
	public int getRowCount() {
		return entries.size();
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		if (rowIndex >= 0 && rowIndex < entries.size()) {
			final EntryUiModel entry = entries.get(rowIndex);
			if (columnIndex == 0) {
				return entry;
			}
		}

		return null;
	}

	public void updatedValueAt(final int currentEditedRow, final int currentEditedColumn) {
		final TableModelEvent updateEvent = new TableModelEvent(this, currentEditedColumn, currentEditedColumn, TableModelEvent.UPDATE);
		for (final TableModelListener listener : listeners) {
			listener.tableChanged(updateEvent);
		}
	}

	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		final Object entryModel = getValueAt(rowIndex, columnIndex);
		return !(entryModel instanceof ParentFolderEntryUiModel);
	}

	@Override
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {

		// strange code with strange behaviour of CellEditor - dont touch
		if (!(aValue instanceof EntryUiModel)) {
			return;
		}

		entries.remove(rowIndex);
		entries.add(rowIndex, (EntryUiModel) aValue);

		final TableModelEvent updateEvent = new TableModelEvent(this, rowIndex, rowIndex, 0, TableModelEvent.UPDATE);
		for (final TableModelListener listener : listeners) {
			listener.tableChanged(updateEvent);
		}

	}

	public void setEntries(final ParentFolderEntryUiModel parentFolderEntryModel, final List<EntryUiModel> newEntries) {
		final int oldSize = entries.size();
		entries.clear();

		final TableModelEvent removeEvent = new TableModelEvent(this, 0, oldSize, 0, TableModelEvent.DELETE);
		for (final TableModelListener listener : listeners) {
			listener.tableChanged(removeEvent);
		}

		if (parentFolderEntryModel != null) {
			entries.add(parentFolderEntryModel);
		}

		for (final EntryUiModel newEntry : newEntries) {
			entries.add(newEntry);
		}

		final int newSize = entries.size();

		final TableModelEvent addedEvent = new TableModelEvent(this, 0, newSize, 0, TableModelEvent.INSERT);
		for (final TableModelListener listener : listeners) {
			listener.tableChanged(addedEvent);
		}
	}

	public void appendEntry(final EntryUiModel entryModel) {
		entries.add(entryModel);

		final int newSize = entries.size();

		final TableModelEvent addedEvent = new TableModelEvent(this, newSize - 1, newSize, 0, TableModelEvent.INSERT);
		for (final TableModelListener listener : listeners) {
			listener.tableChanged(addedEvent);
		}
	}

	public void removeAtIndex(final int rowIndexToRemove) {
		entries.remove(rowIndexToRemove);
		final TableModelEvent addedEvent = new TableModelEvent(this, rowIndexToRemove, rowIndexToRemove, 0, TableModelEvent.DELETE);
		for (final TableModelListener listener : listeners) {
			listener.tableChanged(addedEvent);
		}
	}
}
