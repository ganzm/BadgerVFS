package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import javax.swing.JDialog;
import javax.swing.JFrame;

import ch.eth.jcd.badgers.vfs.ui.desktop.action.SampleAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.NewDiskCreationDialog;

public class DesktopController {

	public void testBlockingAction() {
		// TODO Auto-generated method stub

		WorkerController workerController = WorkerController.getInstance();

		SampleAction sampleAction = new SampleAction();

		workerController.enqueue(sampleAction);
		boolean b = workerController.isBusy();
	}

	public void openCreateNewDiskDialog(JFrame desktop) {

		NewDiskCreationDialog dialog = new NewDiskCreationDialog(desktop);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

}
