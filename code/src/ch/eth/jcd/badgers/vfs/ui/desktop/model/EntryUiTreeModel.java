package ch.eth.jcd.badgers.vfs.ui.desktop.model;

import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

public class EntryUiTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = -7151461530405344783L;

	public EntryUiTreeModel() {
		super(new EntryUiTreeNode("/", true, null));
	}

	public void updateTreeAddChilds(EntryUiTreeNode parent, List<EntryUiTreeNode> childs) {
		for (EntryUiTreeNode child : childs) {
			if (parent == null) {
				insertNodeInto(child, (EntryUiTreeNode) getRoot(), childs.indexOf(child));
			} else {
				insertNodeInto(child, parent, childs.indexOf(child));
			}
		}
	}

	public void removeChildsFromParent(EntryUiTreeNode parent) {
		if (parent != null && parent.getChildCount() > 0) {
			for (int i = 0; i < parent.getChildCount(); i++) {
				removeNodeFromParent((MutableTreeNode) parent.getChildAt(i));
			}
		}

	}

}
