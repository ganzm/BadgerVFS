package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import java.awt.Component;
import java.io.File;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.ListModel;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.BadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.GetRootFolderContentAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.SampleAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiListModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.NewDiskCreationDialog;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public class DesktopController extends BadgerController implements ActionObserver {

	private EntryUiListModel entryListModel = new EntryUiListModel();

	public DesktopController(BadgerViewBase desktopView) {
		super(desktopView);
	}

	public void testBlockingAction() {
		// TODO this is Test CODE

		WorkerController workerController = WorkerController.getInstance();

		SampleAction sampleAction = new SampleAction();

		workerController.enqueue(sampleAction);
		boolean b = workerController.isBusy();
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
		GetRootFolderContentAction getFolderContentAction = new GetRootFolderContentAction();

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
		if (action instanceof GetRootFolderContentAction) {

			GetRootFolderContentAction getRootFolderAction = (GetRootFolderContentAction) action;
			List<EntryUiModel> entries = getRootFolderAction.getRootEntries();

			setCurrentFolder(VFSPath.FILE_SEPARATOR, entries);
		} else {
			SwingUtil.handleError(null, "Unhandled Action " + action);
		}

		updateGUI();
	}

	private void setCurrentFolder(String path, List<EntryUiModel> entries) {

		entryListModel.setEntries(entries);
		updateGUI();
		// TODO
	}

	public boolean isBusy() {
		WorkerController workerController = WorkerController.getInstance();
		if (workerController != null) {
			return workerController.isBusy();
		}
		return false;
	}

	public ListModel<EntryUiModel> getEntryListModel() {
		return entryListModel;
	}
}
