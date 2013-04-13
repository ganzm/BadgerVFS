package ch.eth.jcd.badgers.vfs.ui.desktop.model;


import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class EntryUiTreeNode extends DefaultMutableTreeNode{

	private final EntryUiModel entry;

	public EntryUiTreeNode(Object userObject, boolean allowsChildren, EntryUiModel entry) {
		super(userObject, allowsChildren);
		this.entry = entry;
	}

	public EntryUiModel getUiEntry() {
		return entry;
	}
	
	@Override
	public String toString(){
		return super.toString();
	}
}
