package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;

public class EntryListCellRenderer implements TableCellRenderer {

	private final JLabel label;

	private final Color textSelectionColor = Color.BLACK;
	private final Color backgroundSelectionColor = Color.LIGHT_GRAY;
	private final Color textNonSelectionColor = Color.BLACK;
	private final Color backgroundNonSelectionColor = Color.WHITE;

	public EntryListCellRenderer() {
		label = new JLabel();
		label.setOpaque(true);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		EntryUiModel entry = (EntryUiModel) value;

		label.setIcon(entry.getIcon());
		label.setText(entry.getDisplayName());
		label.setToolTipText(entry.getFullPath());

		if (isSelected) {
			label.setBackground(backgroundSelectionColor);
			label.setForeground(textSelectionColor);
		} else {
			label.setBackground(backgroundNonSelectionColor);
			label.setForeground(textNonSelectionColor);
		}

		return label;
	}
}
