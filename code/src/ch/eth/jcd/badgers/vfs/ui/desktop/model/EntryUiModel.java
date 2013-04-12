package ch.eth.jcd.badgers.vfs.ui.desktop.model;

import javax.swing.ImageIcon;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;

/**
 * Directory or file which is shown in the GUI TODO describe EntryUiModel
 * 
 */
public class EntryUiModel {

	private final VFSEntry entry;

	public EntryUiModel(VFSEntry entry) {
		this.entry = entry;
	}

	public ImageIcon getIcon() {
		return null;

	}

	public String getName() {
		return null;
	}
}
