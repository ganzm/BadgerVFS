package ch.eth.jcd.badgers.vfs.ui.desktop.model;

import javax.swing.tree.DefaultMutableTreeNode;

public class EntryUiTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -6288068772437793999L;

	private final EntryUiModel entry;

	public EntryUiTreeNode(Object userObject, boolean allowsChildren, EntryUiModel entry) {
		super(userObject, allowsChildren);
		this.entry = entry;
	}

	public EntryUiModel getUiEntry() {
		return entry;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
