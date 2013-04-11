package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import javax.swing.JDialog;
import javax.swing.JFrame;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
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
}
