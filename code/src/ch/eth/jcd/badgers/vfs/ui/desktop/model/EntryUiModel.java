package ch.eth.jcd.badgers.vfs.ui.desktop.model;

import java.io.IOException;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
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

	private final boolean directory;

	private boolean isBeeingRenaming = false;

	/**
	 * Static constructor load some images
	 */
	static {
		try {
			fileIcon = ResourceLocator.getResourceAsIcon("images/Document-Blank-icon.png");
			folderIcon = ResourceLocator.getResourceAsIcon("images/Folder-icon.png");
		} catch (final IOException e) {
			LOGGER.error("Error while loading ImageIcons", e);
		}
	}

	/**
	 * Constructor
	 * 
	 * @param entry
	 */
	public EntryUiModel(final VFSEntry entry, final boolean isDirectory) {
		this.entry = entry;
		this.directory = isDirectory;
	}

	public void toggleRename() {
		this.isBeeingRenaming = !isBeeingRenaming;
	}

	public boolean isBeeingRenamed() {
		return isBeeingRenaming;
	}

	public boolean isDirectory() {
		return directory;
	}

	public ImageIcon getIcon() {

		if (directory) {
			return folderIcon;
		} else {
			return fileIcon;
		}
	}

	public String getDisplayName() {
		return entry.getPath().getName();
	}

	public String getFullPath() {
		return entry.getPath().getAbsolutePath();
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
