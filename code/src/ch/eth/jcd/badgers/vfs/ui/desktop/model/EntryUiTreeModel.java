package ch.eth.jcd.badgers.vfs.ui.desktop.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class EntryUiTreeModel extends DefaultTreeModel {

	public EntryUiTreeModel() {
		super(new EntryUiTreeNode("/", true, null));
	}

	public void updateTreeAddChilds(EntryUiTreeNode parent, List<EntryUiTreeNode> childs) {
		for(EntryUiTreeNode child : childs){
			if(parent == null){
				insertNodeInto(child, (EntryUiTreeNode) getRoot(), childs.indexOf(child));
			} else{
				insertNodeInto(child, parent, childs.indexOf(child));
			}
		}
	}

	public void removeChildsFromParent(EntryUiTreeNode parent) {
		if(parent != null && parent.getChildCount() > 0){
			for(int i = 0; i< parent.getChildCount();i++){
				removeNodeFromParent((MutableTreeNode) parent.getChildAt(i));
			}
		}

	}

}
