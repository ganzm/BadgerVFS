package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;

public class EntryListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = -1506119105265047133L;

	private JLabel label;

	private Color textSelectionColor = Color.BLACK;
	private Color backgroundSelectionColor = Color.CYAN;
	private Color textNonSelectionColor = Color.BLACK;
	private Color backgroundNonSelectionColor = Color.WHITE;

	public EntryListCellRenderer() {
		label = new JLabel();
		label.setOpaque(true);
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean expanded) {
		EntryUiModel entry = (EntryUiModel) value;

		label.setIcon(entry.getIcon());
		label.setText(entry.getDisplayName());
		label.setToolTipText(entry.getFullPath());

		if (selected) {
			label.setBackground(backgroundSelectionColor);
			label.setForeground(textSelectionColor);
		} else {
			label.setBackground(backgroundNonSelectionColor);
			label.setForeground(textNonSelectionColor);
		}

		return label;
	}
}
