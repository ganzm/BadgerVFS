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

	public static ImageIcon fileIcon;
	public static ImageIcon folderIcon;

	private final VFSEntry entry;

	private final boolean isDirectory;

	/**
	 * Static constructor load some images
	 */
	static {
		try {
			fileIcon = ResourceLocator.getResourceAsIcon("images/Document-Blank-icon.png");
			folderIcon = ResourceLocator.getResourceAsIcon("images/Folder-icon.png");
		} catch (IOException e) {
			LOGGER.error("Error while loading ImageIcons", e);
		}
	}

	/**
	 * Constructor
	 * 
	 * @param entry
	 */
	public EntryUiModel(VFSEntry entry, boolean isDirectory) {
		this.entry = entry;
		this.isDirectory = isDirectory;
	}

	public void toggleRename() {
		// TODO Auto-generated method stub

	}

	public boolean isDirectory() {
		return isDirectory;
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

	/**
	 * Don't use this in GUI context
	 * 
	 * @return
	 */
	public VFSEntry getEntry() {
		return entry;
	}

	@Override
	public String toString() {
		return "Entry " + getFullPath();
	}

}
