package ch.eth.jcd.badgers.vfs.ui.desktop.model;

import java.io.IOException;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.util.ResourceLocator;

/**
 * Directory or file which is shown in the GUI
 * 
 */
public class EntryUiModel {
	private static final Logger LOGGER = Logger.getLogger(EntryUiModel.class);

	private static ImageIcon fileIcon;
	private static ImageIcon folderIcon;

	private final VFSEntry entry;

	static {
		try {
			fileIcon = ResourceLocator.getResourceAsIcon("images/Document-Blank-icon.png");
			folderIcon = ResourceLocator.getResourceAsIcon("images/Folder-icon.png");
		} catch (IOException e) {
			LOGGER.error("Error while loading ImageIcons", e);
		}
	}

	public EntryUiModel(VFSEntry entry) {
		this.entry = entry;
	}

	public ImageIcon getIcon() {

		if (entry.isDirectory()) {
			return folderIcon;
		} else {
			return fileIcon;
		}
	}

	public String getDisplayName() {
		try {
			return entry.getPath().getName();
		} catch (VFSException ex) {
			LOGGER.error("", ex);
			return "ERROR";
		}
	}

	public String getFullPath() {
		try {
			return entry.getPath().getAbsolutePath();
		} catch (VFSException ex) {
			LOGGER.error("", ex);
			return "ERROR";
		}
	}
}
