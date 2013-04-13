package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.Color;
import java.awt.Component;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiTreeNode;
import ch.eth.jcd.badgers.vfs.util.ResourceLocator;

public class EntryTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final Logger LOGGER = Logger.getLogger(EntryTreeCellRenderer.class);

	private static final long serialVersionUID = -1506119105265047133L;

	private JLabel label;

	private final Color textSelectionColor = Color.BLACK;
	private final Color backgroundSelectionColor = Color.CYAN;
	private final Color textNonSelectionColor = Color.BLACK;
	private final Color backgroundNonSelectionColor = Color.WHITE;

	TreeCellRenderer renderer; // The renderer we are a wrapper for

	public EntryTreeCellRenderer() {
		super();
		// label = new JLabel();
		// label.setOpaque(true);
	}

	public static ImageIcon folderClosed;
	public static ImageIcon folderOpen;

	/**
	 * Static constructor load some images
	 */
	static {
		try {
			folderClosed = ResourceLocator.getResourceAsIcon("images/Folder-icon.png");
			folderOpen = ResourceLocator.getResourceAsIcon("images/Folder-Open-icon.png");
		} catch (IOException e) {
			LOGGER.error("Error while loading ImageIcons", e);
		}
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean bSelected, boolean bExpanded, boolean bLeaf, int iRow, boolean bHasFocus) {
		// EntryUiModel entry = (EntryUiModel) value;

		/*
		 * entryPanel = new EntryPanel(); // entryPanel.setOpaque(true);
		 * 
		 * entryPanel.setIcon(entry.getIcon()); entryPanel.setText(entry.getDisplayName()); entryPanel.setToolTipText(entry.getFullPath());
		 * 
		 * if (selected) { entryPanel.setBackground(backgroundSelectionColor); entryPanel.setForeground(textSelectionColor); } else {
		 * entryPanel.setBackground(backgroundNonSelectionColor); entryPanel.setForeground(textNonSelectionColor); }
		 */
		label = new JLabel();
		EntryUiTreeNode node = (EntryUiTreeNode) value;
		String labelText;
		labelText = node.getUiEntry() == null ? node.toString() : node.getUiEntry().getDisplayName();

		label.setText(labelText);
		label.setToolTipText(labelText);
		if (node.isLeaf()) {
			label.setIcon(folderClosed);
		} else {
			label.setIcon(folderOpen);
		}

		if (bSelected) {
			label.setBackground(backgroundSelectionColor);
			label.setForeground(textSelectionColor);
		} else {
			label.setBackground(backgroundNonSelectionColor);
			label.setForeground(textNonSelectionColor);
		}
		return label;
		// return super.getTreeCellRendererComponent(tree, label,
		// bSelected, bExpanded, bLeaf, iRow, bHasFocus);
	}
}
