package ch.eth.jcd.badgers.vfs.remote.model;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class LinkedDiskTableModel implements TableModel {

	private final List<TableModelListener> listeners = new ArrayList<>();

	private final List<LinkedDisk> disks;
	private static final char[] UNITS = { 'B', 'K', 'M', 'G', 'T', 'P' };

	public LinkedDiskTableModel(final List<LinkedDisk> disks) {
		this.disks = disks;
	}

	public List<LinkedDisk> getEntries() {
		return disks;
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
		switch (columnIndex) {
		case 0:
			return String.class;
		case 1:
			return String.class;
		case 2:
			return String.class;
		case 3:
			return String.class;
		default:
			return null;
		}
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public String getColumnName(final int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "Filename";
		case 1:
			return "Size";
		case 2:
			return "Encrypted";
		case 3:
			return "Compression";
		default:
			return "";
		}
	}

	@Override
	public int getRowCount() {
		return disks.size();
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		if (rowIndex >= 0 && rowIndex < disks.size()) {
			final LinkedDisk disk = disks.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return disk.getDisplayName();
			case 1:
				return getFormattedSize(disk.getDiskConfig().getMaximumSize());
			case 2:
				return disk.getDiskConfig().getEncryptionAlgorithm().name();
			case 3:
				return disk.getDiskConfig().getCompressionAlgorithm().name();
			default:
				return "";
			}
		}

		return null;
	}

	private String getFormattedSize(final long size) {
		double tmpSize = size;
		int unit = 0;
		while (tmpSize > 1024) {
			tmpSize = tmpSize / 1024;
			unit++;
		}
		final DecimalFormat df = new DecimalFormat("####.#");

		return df.format(tmpSize) + (unit < UNITS.length ? UNITS[unit] : "XL");
	}

	public void updatedValueAt(final int currentEditedRow, final int currentEditedColumn) {
		final TableModelEvent updateEvent = new TableModelEvent(this, currentEditedColumn, currentEditedColumn, TableModelEvent.UPDATE);
		for (final TableModelListener listener : listeners) {
			listener.tableChanged(updateEvent);
		}
	}

	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		return false;
	}

	@Override
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {

		// strange code with strange behaviour of CellEditor - dont touch
		if (!(aValue instanceof LinkedDisk)) {
			return;
		}

		disks.remove(rowIndex);
		disks.add(rowIndex, (LinkedDisk) aValue);

		final TableModelEvent updateEvent = new TableModelEvent(this, rowIndex, rowIndex, 0, TableModelEvent.UPDATE);
		for (final TableModelListener listener : listeners) {
			listener.tableChanged(updateEvent);
		}

	}
}
