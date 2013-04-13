package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;

public class EntryListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = -1506119105265047133L;

	// private JLabel label;
	private EntryPanel entryPanel;

	private Color textSelectionColor = Color.BLACK;
	private Color backgroundSelectionColor = Color.CYAN;
	private Color textNonSelectionColor = Color.BLACK;
	private Color backgroundNonSelectionColor = Color.WHITE;

	public EntryListCellRenderer() {
		// label = new JLabel();
		// label.setOpaque(true);
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean expanded) {
		EntryUiModel entry = (EntryUiModel) value;

		entryPanel = new EntryPanel();
		// entryPanel.setOpaque(true);

		entryPanel.setIcon(entry.getIcon());
		entryPanel.setText(entry.getDisplayName());
		entryPanel.setToolTipText(entry.getFullPath());

		if (selected) {
			entryPanel.setBackground(backgroundSelectionColor);
			entryPanel.setForeground(textSelectionColor);
		} else {
			entryPanel.setBackground(backgroundNonSelectionColor);
			entryPanel.setForeground(textNonSelectionColor);
		}

		return entryPanel;
	}
}
