package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import java.awt.Component;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.BadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.CreateFolderAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.DeleteEntryAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.GetFolderContentAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.GetTreeContentAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ImportAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.RenameEntryAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.BadgerFileExtensionFilter;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryTableModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiTreeModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiTreeNode;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.ParentFolderEntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.ImportDialog;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.NewDiskCreationDialog;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public class DesktopController extends BadgerController implements ActionObserver {

	private static final Logger LOGGER = Logger.getLogger(DesktopController.class);

	private final EntryTableModel entryTableModel = new EntryTableModel();

	private final EntryUiTreeModel entryTreeModel = new EntryUiTreeModel();

	private VFSPath currentFolder;

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
		WorkerController controller = WorkerController.setupWorker(diskManager);
		controller.addActionObserver(this);

		loadRootFolder();

		updateGUI();
	}

	public void createDisk(DiskConfiguration config) throws VFSException {
		if (!isInManagementMode()) {
			throw new VFSException("Cannot create new Disk - close current disk first");
		}

		VFSDiskManagerFactory factory = VFSDiskManagerFactory.getInstance();
		VFSDiskManager diskManager = factory.createDiskManager(config);

		WorkerController controller = WorkerController.setupWorker(diskManager);
		controller.addActionObserver(this);

		loadRootFolder();

		updateGUI();
	}

	private void loadRootFolder() {
		GetFolderContentAction getFolderContentAction = new GetFolderContentAction();
		GetTreeContentAction getTreeContentAction = new GetTreeContentAction();

		WorkerController workerController = WorkerController.getInstance();
		workerController.enqueue(getFolderContentAction);
		workerController.enqueue(getTreeContentAction);
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
			GetFolderContentAction getFolderAction = (GetFolderContentAction) action;
			ParentFolderEntryUiModel parentFolderEntryModel = getFolderAction.getParentFolderEntryModel();
			List<EntryUiModel> entries = getFolderAction.getEntries();

			setCurrentFolder(getFolderAction.getFolderPath(), parentFolderEntryModel, entries);
		} else if (action instanceof GetTreeContentAction) {
			GetTreeContentAction getTreeAction = (GetTreeContentAction) action;
			EntryUiTreeNode parent = getTreeAction.getParent();
			List<EntryUiModel> entries = getTreeAction.getEntries();
			List<EntryUiTreeNode> treeEntries = new LinkedList<>();
			for (EntryUiModel entry : entries) {
				treeEntries.add(new EntryUiTreeNode(entry.toString(), true, entry));
			}
			entryTreeModel.removeChildsFromParent(parent);
			entryTreeModel.updateTreeAddChilds(parent, treeEntries);
			entryTreeModel.reload();
		} else if (action instanceof DeleteEntryAction) {
			DeleteEntryAction deleteAction = (DeleteEntryAction) action;
			entryTableModel.removeAtIndex(deleteAction.getRowIndexToRemove());
		} else if (action instanceof CreateFolderAction) {
			CreateFolderAction createAction = (CreateFolderAction) action;
			EntryUiModel entryModel = new EntryUiModel(createAction.getNewFolder(), true);
			entryTableModel.appendEntry(entryModel);
		} else if (action instanceof RenameEntryAction) {
			RenameEntryAction renameAction = (RenameEntryAction) action;
			entryTableModel.setValueAt(renameAction.getEntryModel(), renameAction.getEditedRowIndex(), 0);
		} else {
			SwingUtil.handleError(null, "Unhandled Action " + action);
		}

		updateGUI();
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

	public EntryUiTreeModel getEntryTreeModel() {
		return entryTreeModel;
	}

	public void openTree(EntryUiTreeNode entry) {
		GetTreeContentAction action = new GetTreeContentAction(entry);
		WorkerController workerController = WorkerController.getInstance();
		workerController.enqueue(action);

	}

	public void openEntry(EntryUiModel entry) {
		GetFolderContentAction action = new GetFolderContentAction(entry);
		WorkerController workerController = WorkerController.getInstance();
		workerController.enqueue(action);

	}

	public void startDelete(EntryUiModel entry, int editedRow) {
		DeleteEntryAction action = new DeleteEntryAction(entry, editedRow);
		WorkerController.getInstance().enqueue(action);
	}

	public void StartRenameEntry(EntryUiModel currentEditedValue, int editedRow, String newEntryName) {
		RenameEntryAction action = new RenameEntryAction(currentEditedValue, editedRow, newEntryName);
		WorkerController.getInstance().enqueue(action);
	}

	private String getUniquieFolderName(List<EntryUiModel> entries) {
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

	public void startCreatenewFolder() {
		List<EntryUiModel> entries = entryTableModel.getEntries();
		String newFolderName = getUniquieFolderName(entries);
		startCreateNewFolder(newFolderName);
	}

	public void startCreateNewFolder(String name) {
		CreateFolderAction action = new CreateFolderAction(currentFolder, name);
		WorkerController.getInstance().enqueue(action);
	}

	public void startImportFromHostFs(String sourcePath) {

		ImportAction action = new ImportAction(sourcePath, currentFolder);
		WorkerController.getInstance().enqueue(action);

	}
}
