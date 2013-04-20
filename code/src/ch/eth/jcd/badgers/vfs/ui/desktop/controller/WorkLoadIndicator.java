package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import java.util.Timer;

import ch.eth.jcd.badgers.vfs.ui.desktop.view.PleaseWaitDialog;

public class WorkLoadIndicator {

	private WorkerController workerController;
	private PleaseWaitDialog dialog;
	private Timer timer;

	public WorkLoadIndicator(WorkerController workerController) {
		this.workerController = workerController;
		this.dialog = new PleaseWaitDialog();

		this.timer = new Timer();
	}

	public void jobEnqueued() {
		//dialog.setVisible(true);

		// timer.schedule(new TimerTask() {
		//
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		//
		// }
		// }, 500);
	}

	public void actionFinished() {
		// TODO Auto-generated method stub

		//dialog.setVisible(false);
	}

}
