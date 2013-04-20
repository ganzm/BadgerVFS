package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import java.awt.EventQueue;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.ui.desktop.action.BadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.PleaseWaitDialog;

public class WorkLoadIndicator {

	private static final Logger LOGGER = Logger.getLogger(WorkLoadIndicator.class);

	private final PleaseWaitDialog dialog;
	private final Timer timer;

	private int jobPerforming = 0;

	private final Object obj = new Object();

	public WorkLoadIndicator() {
		this.dialog = new PleaseWaitDialog();
		this.timer = new Timer();
	}

	public void dispose() {
		timer.cancel();

		synchronized (obj) {
			jobPerforming--;
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					dialog.dispose();
				};
			});
		}
	}

	public void jobEnqueued(final BadgerAction action) {
		LOGGER.debug("job enqueued");
		dialog.setCurrentAction(action);
		synchronized (obj) {
			jobPerforming++;
		}

		if (action.needsToLockGui()) {
			timer.schedule(new WorkLoadTimerTask(), 500);
		}

	}

	public void actionFinished() {
		LOGGER.debug("action finished");

		synchronized (obj) {
			jobPerforming--;
			if (dialog.isVisible()) {
				startSetInvisible();
			}
		}
	}

	private void startSetInvisible() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				dialog.setVisible(false);
			};
		});
	}

	private class WorkLoadTimerTask extends TimerTask {
		@Override
		public void run() {
			synchronized (obj) {
				if (jobPerforming > 0) {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							dialog.setVisible(true);
						}
					});
				}
			}
		}
	};
}
