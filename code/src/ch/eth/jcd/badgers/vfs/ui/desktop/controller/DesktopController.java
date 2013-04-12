package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import java.awt.Component;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.GetRootFolderContentAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.SampleAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.NewDiskCreationDialog;

public class DesktopController extends BadgerController {

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

	public void openCreateNewDiskDialog(JFrame desktop) {

		NewDiskCreationDialog dialog = new NewDiskCreationDialog(desktop, this);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

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

	private void openDisk(String path) throws VFSException {
		if (!isInManagementMode()) {
			throw new VFSException("Cannot open Disk on " + path + " - close current disk first");
		}

		DiskConfiguration config = new DiskConfiguration();
		config.setHostFilePath(path);

		VFSDiskManagerFactory factory = VFSDiskManagerFactory.getInstance();
		VFSDiskManager diskManager = factory.openDiskManager(config);
		WorkerController.setupWorker(diskManager);

		GetRootFolderContentAction getFolderContentAction = new GetRootFolderContentAction();

		WorkerController workerController = WorkerController.getInstance();
		workerController.enqueue(getFolderContentAction);

		boolean b = workerController.isBusy();

		updateGUI();
	}

	public void createDisk(DiskConfiguration config) throws VFSException {
		if (!isInManagementMode()) {
			throw new VFSException("Cannot create new Disk - close current disk first");
		}

		VFSDiskManagerFactory factory = VFSDiskManagerFactory.getInstance();
		VFSDiskManager diskManager = factory.createDiskManager(config);
		WorkerController.setupWorker(diskManager);

		updateGUI();
	}

	public boolean isInManagementMode() {
		return WorkerController.getInstance() == null;
	}

	public void closeDisk(JFrame desktopFrame) throws VFSException {
		if (isInManagementMode()) {
			throw new VFSException("Cannot close new Disk - no current disk opened");
		}

		WorkerController.disposeWorker();
		updateGUI();
	}
}
