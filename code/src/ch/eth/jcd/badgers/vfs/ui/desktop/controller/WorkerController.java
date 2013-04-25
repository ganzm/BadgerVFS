package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.DiskAction;

/**
 * This class handles access to the VFSDiskManager
 */
public abstract class WorkerController implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(WorkerController.class);

	/**
	 * Queue contains unprocesses jobs
	 */
	private final LinkedBlockingQueue<AbstractBadgerAction> actionQueue = new LinkedBlockingQueue<>();

	private boolean running = false;

	public WorkerController() {

	}

	public void startWorkerController() {
		final Thread controllerThread = new Thread(this);
		controllerThread.setName(this.getClass().getSimpleName());
		controllerThread.start();
	}

	protected void enqueue(final AbstractBadgerAction action) {
		try {
			actionQueue.put(action);
		} catch (final InterruptedException e) {
			LOGGER.error("error putting into queue", e);
		}
	}

	@Override
	public void run() {
		try {
			LOGGER.debug("Starting WorkerController Thread");
			running = true;
			AbstractBadgerAction action = null;

			while (running) {
				try {
					action = actionQueue.take();
					performAction(action);
				} catch (final Exception ex) {
					LOGGER.error("Error while performing Action " + action, ex);
				}
			}
		} finally {
			LOGGER.debug("Shutdown WorkerController Thread");
		}
	}

	protected abstract void performAction(final AbstractBadgerAction action);

	protected void actionFailed(final AbstractBadgerAction action, final Exception e) {
		final ActionObserver obs = action.getActionObserver();
		if (obs != null) {
			try {
				obs.onActionFailed(action, e);
			} catch (final Exception ex) {
				LOGGER.error("Error while notifying Observer " + obs, ex);
			}
		}
	}

	protected void actionFinished(final AbstractBadgerAction action) {
		final ActionObserver obs = action.getActionObserver();
		if (obs != null) {
			try {
				obs.onActionFinished(action);
			} catch (final Exception ex) {
				LOGGER.error("Error while notifying Observer" + obs, ex);
			}
		}
	}

	public void dispose() {
		running = false;
		final DiskAction noop = new DiskAction(null) {
			@Override
			public void runDiskAction(final VFSDiskManager diskManager) throws VFSException {
				// intentionally does nothing
			}
		};

		enqueue(noop);

	}
}
