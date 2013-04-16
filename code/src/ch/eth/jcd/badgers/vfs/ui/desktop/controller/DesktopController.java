package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.BadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.CopyAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.CreateFolderAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.CutAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.DeleteEntryAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ExportAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.GetFolderContentAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ImportAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.RenameEntryAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.BadgerFileExtensionFilter;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryTableModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.ParentFolderEntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.DiskSpaceDialog;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.ImportDialog;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.InfoDialog;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.NewDiskCreationDialog;
import ch.eth.jcd.badgers.vfs.util.Pair;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public class DesktopController extends BadgerController implements ActionObserver {
	public enum ClipboardAction {
		COPY, CUT
	}

	private static final Logger LOGGER = Logger.getLogger(DesktopController.class);

	private final EntryTableModel entryTableModel = new EntryTableModel();

	private VFSPath currentFolder;

	private Pair<ClipboardAction, EntryUiModel> clipboard;

	public DesktopController(BadgerViewBase desktopView) {
		super(desktopView);
	}

	/**
	 * shows a blocking dialog which lets you create a new VFS Disk
	 * 
	 * @param desktop
	 */
	public void openCreateNewDiskDialog(JFrame desktop) {
		NewDiskCreationDialog dialog = new NewDiskCreationDialog(desktop, this);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openImportDialog(JFrame desktop) {
		ImportDialog dialog = new ImportDialog(desktop, this, currentFolder.getAbsolutePath());
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openDiskSpaceDialog(JFrame desktop) {
		DiskSpaceDialog dialog = new DiskSpaceDialog(desktop);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openInfoDialog(JFrame desktop) {
		InfoDialog dialog = new InfoDialog(desktop);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	/**
	 * You have clicked on the Disk / Open menu item
	 * 
	 * @param parent
	 * @throws VFSException
	 */
	public void openFileChooserForDiskOpen(Component parent) throws VFSException {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle("Open Badger Disk");
		fileChooser.setMultiSelectionEnabled(false);
		FileFilter bfsType = new BadgerFileExtensionFilter();
		fileChooser.addChoosableFileFilter(bfsType);
		fileChooser.setFileFilter(bfsType);
		fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

		int returnVal = fileChooser.showOpenDialog(parent);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			openDisk(selectedFile.getAbsolutePath());
		}
	}

	/**
	 * Actualy open a disk You acknowledged the Disk / Open FileChooser Dialog
	 * 
	 * @param path
	 * @throws VFSException
	 */
	private void openDisk(String path) throws VFSException {
		if (!isInManagementMode()) {
			throw new VFSException("Cannot open Disk on " + path + " - close current disk first");
		}

		LOGGER.debug("Opening Disk from Path " + path);

		DiskConfiguration config = new DiskConfiguration();
		config.setHostFilePath(path);

		VFSDiskManagerFactory factory = VFSDiskManagerFactory.getInstance();
		VFSDiskManager diskManager = factory.openDiskManager(config);

		// create WorkerController
		WorkerController.setupWorker(diskManager);

		loadRootFolder();

		updateGUI();
	}

	public void createDisk(DiskConfiguration config) throws VFSException {
		if (!isInManagementMode()) {
			throw new VFSException("Cannot create new Disk - close current disk first");
		}

		VFSDiskManagerFactory factory = VFSDiskManagerFactory.getInstance();
		VFSDiskManager diskManager = factory.createDiskManager(config);

		WorkerController.setupWorker(diskManager);

		loadRootFolder();

		updateGUI();
	}

	private void loadRootFolder() {
		GetFolderContentAction getFolderContentAction = new GetFolderContentAction(this);

		WorkerController workerController = WorkerController.getInstance();
		workerController.enqueue(getFolderContentAction);
	}

	public boolean isInManagementMode() {
		return WorkerController.getInstance() == null;
	}

	/**
	 * You have clicked on the Disk / Close menu item
	 * 
	 * @param desktopFrame
	 * @throws VFSException
	 */
	public void closeDisk(JFrame desktopFrame) throws VFSException {
		if (isInManagementMode()) {
			throw new VFSException("Cannot close new Disk - no current disk opened");
		}

		WorkerController.disposeWorker();
		updateGUI();
	}

	@Override
	public void onActionFailed(BadgerAction action, VFSException e) {
		SwingUtil.handleException(null, e);
		updateGUI();
	}

	@Override
	public void onActionFinished(BadgerAction action) {
		if (action instanceof GetFolderContentAction) {
			getFolderContentActionFinished((GetFolderContentAction) action);
		} else if (action instanceof DeleteEntryAction) {
			// after deletion of multiple items we cannot operate anymore with table indices
			GetFolderContentAction reloadCurrentFolderAction = new GetFolderContentAction(this, currentFolder);
			WorkerController.getInstance().enqueue(reloadCurrentFolderAction);
		} else if (action instanceof CreateFolderAction) {
			CreateFolderAction createAction = (CreateFolderAction) action;
			EntryUiModel entryModel = new EntryUiModel(createAction.getNewFolder(), true);
			entryTableModel.appendEntry(entryModel);
		} else if (action instanceof ImportAction) {
			// reload current folder after import
			GetFolderContentAction reloadCurrentFolderAction = new GetFolderContentAction(this, currentFolder);
			WorkerController.getInstance().enqueue(reloadCurrentFolderAction);
		} else if (action instanceof RenameEntryAction) {
			RenameEntryAction renameAction = (RenameEntryAction) action;
			entryTableModel.setValueAt(renameAction.getEntryModel(), renameAction.getEditedRowIndex(), 0);
		} else if (action instanceof ExportAction) {
			ExportAction exportAction = (ExportAction) action;
			JOptionPane.showMessageDialog(exportAction.getDesktopFrame(), "Successfully exported " + exportAction.getEntries() + " to "
					+ exportAction.getDestination().getAbsolutePath());
		} else if (action instanceof CopyAction) {
			GetFolderContentAction reloadCurrentFolderAction = new GetFolderContentAction(this, currentFolder);
			WorkerController.getInstance().enqueue(reloadCurrentFolderAction);
		} else if (action instanceof CutAction) {
			GetFolderContentAction reloadCurrentFolderAction = new GetFolderContentAction(this, currentFolder);
			WorkerController.getInstance().enqueue(reloadCurrentFolderAction);
		} else {
			LOGGER.debug("Action " + action.getClass().getName() + " not handled in " + this.getClass().getName());
		}

		updateGUI();
	}

	private void getFolderContentActionFinished(GetFolderContentAction getFolderAction) {
		ParentFolderEntryUiModel parentFolderEntryModel = getFolderAction.getParentFolderEntryModel();
		List<EntryUiModel> entries = getFolderAction.getEntries();
		setCurrentFolder(getFolderAction.getFolderPath(), parentFolderEntryModel, entries);
	}

	private void setCurrentFolder(VFSPath path, ParentFolderEntryUiModel parentFolderEntryModel, List<EntryUiModel> entries) {
		entryTableModel.setEntries(parentFolderEntryModel, entries);
		this.currentFolder = path;
		updateGUI();
	}

	public boolean isBusy() {
		WorkerController workerController = WorkerController.getInstance();
		if (workerController != null) {
			return workerController.isBusy();
		}
		return false;
	}

	public EntryTableModel getEntryTableModel() {
		return entryTableModel;
	}

	public void openEntry(EntryUiModel entry) {
		GetFolderContentAction action = new GetFolderContentAction(this, entry);
		WorkerController workerController = WorkerController.getInstance();
		workerController.enqueue(action);
	}

	public void startDelete(List<EntryUiModel> entries) {
		DeleteEntryAction action = new DeleteEntryAction(this, entries);
		WorkerController.getInstance().enqueue(action);
	}

	public void startRenameEntry(EntryUiModel currentEditedValue, int editedRow, String newEntryName) {
		RenameEntryAction action = new RenameEntryAction(this, currentEditedValue, editedRow, newEntryName);
		WorkerController.getInstance().enqueue(action);
	}

	private String getUniqueFolderName(List<EntryUiModel> entries) {
		String praefix = "NewFolder";
		String newFolderName = praefix;
		int count = 0;
		boolean isUnique;

		do {
			isUnique = true;
			for (EntryUiModel entry : entries) {
				if (newFolderName.equals(entry.getDisplayName())) {
					isUnique = false;
					newFolderName = praefix + (++count);

					// break for loop
					break;
				}
			}
		} while (!isUnique);

		return newFolderName;
	}

	public void startCreateNewFolder() {
		List<EntryUiModel> entries = entryTableModel.getEntries();
		String newFolderName = getUniqueFolderName(entries);
		startCreateNewFolder(newFolderName);
	}

	public void startCreateNewFolder(String name) {
		CreateFolderAction action = new CreateFolderAction(this, currentFolder, name);
		WorkerController.getInstance().enqueue(action);
	}

	public void startImportFromHostFs(String sourcePath, String targetPath) {
		ImportAction action = new ImportAction(this, sourcePath, targetPath);
		WorkerController.getInstance().enqueue(action);
	}

	public String getCurrentFolderAsString() {
		if (currentFolder == null) {
			return "";
		}
		return currentFolder.getAbsolutePath();
	}

	public void startExport(JFrame desktopFrame, List<EntryUiModel> entries) {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Choose File to export to");
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setFileSelectionMode(entries.size() > 1 || (entries.size() == 1 && entries.get(0).isDirectory()) ? JFileChooser.DIRECTORIES_ONLY
				: JFileChooser.FILES_ONLY);
		int returnVal = fc.showDialog(desktopFrame, "Ok");
		if (returnVal == JFileChooser.APPROVE_OPTION) {

			File selected = fc.getSelectedFile();
			List<VFSEntry> vfsEntries = new ArrayList<VFSEntry>(entries.size());
			for (EntryUiModel uiEntry : entries) {
				vfsEntries.add(uiEntry.getEntry());
			}

			if (selected.isDirectory()
					|| !selected.exists()
					|| JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(desktopFrame,
							"The selected file already exists, do you really want do overwrite it?")) {
				ExportAction action = new ExportAction(this, vfsEntries, selected, desktopFrame);
				WorkerController.getInstance().enqueue(action);

			}
		}
	}

	public void copyToClipboard(EntryUiModel entry) {
		LOGGER.debug("Adding " + entry.getDisplayName() + " to clipboard for copy");
		clipboard = new Pair<ClipboardAction, EntryUiModel>(ClipboardAction.COPY, entry);
	}

	public void cutToClipboard(EntryUiModel entry) {
		LOGGER.debug("Adding " + entry.getDisplayName() + " to clipboard for cut");
		clipboard = new Pair<ClipboardAction, EntryUiModel>(ClipboardAction.CUT, entry);
	}

	/**
	 * pastes clipboard to <code>toFolder</code>. toFolder is allowed to be null and thus the "currentFolder" is taken!
	 * 
	 * @param toFolder
	 */
	public void pasteFromClipboardTo(EntryUiModel toFolder) {

		if (clipboard == null || (toFolder != null && !toFolder.getEntry().isDirectory())) {
			return;
		}
		try {
			VFSEntry destinationFolder = toFolder != null ? toFolder.getEntry() : currentFolder.getVFSEntry();

			switch (clipboard.getFirst()) {
			case COPY:
				CopyAction copy = new CopyAction(this, clipboard.getSecond().getEntry(), destinationFolder);
				WorkerController.getInstance().enqueue(copy);
				break;
			case CUT:
				VFSEntry cutSource = clipboard.getSecond().getEntry();
				String cutSourcePath = cutSource.getPath().getAbsolutePath();

				if (destinationFolder.getPath().getAbsolutePath().contains(cutSourcePath)) {
					SwingUtil.showWarning(null, "No can do! Target folder is a descendant of cut source folder");
					return;
				}

				CutAction cut = new CutAction(this, clipboard.getSecond().getEntry(), destinationFolder);
				WorkerController.getInstance().enqueue(cut);
				break;
			}
			clipboard = null;
		} catch (VFSException e) {
			LOGGER.error("Erro during clipoard action", e);
		}
	}

	public void createNewFolderFromContextMenu(EntryUiModel entry) {
		GetFolderContentAction action = new GetFolderContentAction(new ActionObserver() {

			@Override
			public void onActionFinished(BadgerAction action) {
				getFolderContentActionFinished((GetFolderContentAction) action);
				startCreateNewFolder();
			}

			@Override
			public void onActionFailed(BadgerAction action, VFSException e) {
				SwingUtil.handleException(null, e);
				updateGUI();
			}
		}, entry);

		WorkerController workerController = WorkerController.getInstance();
		workerController.enqueue(action);
	}

	public void importFromContextMenu(EntryUiModel entry, JFrame parent) {
		final JFrame thisParent = parent;
		GetFolderContentAction action = new GetFolderContentAction(new ActionObserver() {

			@Override
			public void onActionFinished(BadgerAction action) {
				getFolderContentActionFinished((GetFolderContentAction) action);
				openImportDialog(thisParent);
			}

			@Override
			public void onActionFailed(BadgerAction action, VFSException e) {
				SwingUtil.handleException(null, e);
				updateGUI();
			}
		}, entry);

		WorkerController workerController = WorkerController.getInstance();
		workerController.enqueue(action);
	}

	public EntryUiModel getParentFolderEntry() {
		try {
			return new ParentFolderEntryUiModel(currentFolder.getVFSEntry());
		} catch (VFSException e) {
			LOGGER.error("error getting parentFolder entry", e);
		}
		return null;
	}

}
