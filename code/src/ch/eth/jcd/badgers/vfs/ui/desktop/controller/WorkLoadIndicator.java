package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import java.awt.EventQueue;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.DiskAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.PleaseWaitDialog;

public class WorkLoadIndicator {

	private static final Logger LOGGER = Logger.getLogger(WorkLoadIndicator.class);

	private final PleaseWaitDialog dialog;
	private final Timer timer;

	private int jobPerforming = 0;

	public WorkLoadIndicator() {
		this.dialog = new PleaseWaitDialog();
		this.timer = new Timer();
	}

	public void dispose() {
		timer.cancel();

		jobPerforming--;
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				dialog.dispose();
			};
		});
	}

	public void actionEnqueued(final DiskAction action) {
		LOGGER.debug("action enqueued " + action.getClass().getSimpleName());
		dialog.setCurrentAction(action);

		jobPerforming++;
		if (action.needsToLockGui()) {
			try {
				timer.schedule(new WorkLoadTimerTask(), 500);
			} catch (IllegalStateException ile) {
				LOGGER.error("error while scheduling task on workload indicator", ile);
			}
		}
	}

	public void actionFinished(DiskAction action) {
		LOGGER.debug("action finished " + action.getClass().getSimpleName());

		jobPerforming--;
		startSetInvisible();
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
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (jobPerforming > 0) {
						dialog.setVisible(true);
					}
				}
			});
		}
	};
}
