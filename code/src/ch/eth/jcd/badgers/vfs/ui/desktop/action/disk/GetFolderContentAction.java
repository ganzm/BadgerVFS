package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import java.util.ArrayList;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.ParentFolderEntryUiModel;

/**
 * This Action loads child entries from a specific folder
 * 
 */
public class GetFolderContentAction extends DiskAction {

	private List<EntryUiModel> uiEntries;

	private ParentFolderEntryUiModel parentFolder;

	/**
	 * folder for which we want to load entries
	 */
	private VFSPath vfsFolderPath;

	/**
	 * 
	 * @param folderEntryModel
	 *            folder for which we want to load entries
	 */
	public GetFolderContentAction(ActionObserver actionObserver, EntryUiModel folderEntryModel) {
		super(actionObserver);
		this.vfsFolderPath = folderEntryModel.getEntry().getPath();
	}

	public GetFolderContentAction(ActionObserver actionObserver, VFSPath vfsFolderPath) {
		super(actionObserver);
		this.vfsFolderPath = vfsFolderPath;
	}

	/**
	 * Use this constructor to load from root directory
	 */
	public GetFolderContentAction(ActionObserver actionObserver) {
		super(actionObserver);
		this.vfsFolderPath = null;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {

		VFSEntry folder;
		if (vfsFolderPath == null) {
			folder = diskManager.getRoot();
			vfsFolderPath = folder.getPath();
		} else {
			folder = vfsFolderPath.getVFSEntry();
		}

		VFSEntry parentEntry = folder.getParent();

		if (parentEntry != folder) {
			// we are not at the root
			parentFolder = new ParentFolderEntryUiModel(folder.getParent());
		}

		List<VFSEntry> entries = folder.getChildren();
		uiEntries = new ArrayList<EntryUiModel>();
		for (VFSEntry entry : entries) {
			uiEntries.add(new EntryUiModel(entry, entry.isDirectory()));
		}
	}

	public List<EntryUiModel> getEntries() {
		return uiEntries;
	}

	public String getFolderPathString() {
		return vfsFolderPath.getAbsolutePath();
	}

	public VFSPath getFolderPath() {
		return vfsFolderPath;
	}

	public ParentFolderEntryUiModel getParentFolderEntryModel() {
		return parentFolder;
	}
}
