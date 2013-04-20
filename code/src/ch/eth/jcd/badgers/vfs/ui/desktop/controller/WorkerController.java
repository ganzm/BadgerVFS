package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.BadgerAction;

/**
 * This class handles access to the VFSDiskManager
 */
public class WorkerController implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(WorkerController.class);

	/**
	 * singleton instance
	 */
	private static WorkerController instance = null;

	private final VFSDiskManager diskManager;

	private final WorkLoadIndicator workLoadIndicator;

	/**
	 * Queue contains unprocesses jobs
	 */
	private final LinkedBlockingQueue<BadgerAction> actionQueue = new LinkedBlockingQueue<>();

	private boolean running = false;

	public WorkerController(final VFSDiskManager diskManager) {
		this.diskManager = diskManager;
		this.workLoadIndicator = new WorkLoadIndicator();
	}

	public static WorkerController setupWorker(final VFSDiskManager diskManager) {
		if (instance != null) {
			throw new VFSRuntimeException("WorkerController already instantiated");
		}
		instance = new WorkerController(diskManager);
		instance.startWorkerController();

		return instance;
	}

	public static void disposeWorker() {
		if (instance != null) {
			instance.dispose();
			instance = null;
		}
	}

	public static WorkerController getInstance() {
		return instance;
	}

	public void startWorkerController() {
		final Thread controllerThread = new Thread(this);
		controllerThread.setName("WorkerController");
		controllerThread.start();
	}

	public void enqueue(final BadgerAction action) {
		workLoadIndicator.jobEnqueued(action);
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
			BadgerAction action = null;

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

	private void performAction(final BadgerAction action) {
		try {
			LOGGER.info("Perform Action " + action);
			try {
				action.runDiskAction(diskManager);
			} finally {
				workLoadIndicator.actionFinished();
			}
			LOGGER.info("Finished Action " + action);
			actionFinished(action);
		} catch (final VFSException | RuntimeException e) {
			LOGGER.error("", e);
			actionFailed(action, e);
		}
	}

	private void actionFailed(final BadgerAction action, final Exception e) {
		final ActionObserver obs = action.getActionObserver();
		if (obs != null) {
			try {
				obs.onActionFailed(action, e);
			} catch (final Exception ex) {
				LOGGER.error("Error while notifying Observer " + obs, ex);
			}
		}
	}

	private void actionFinished(final BadgerAction action) {
		final ActionObserver obs = action.getActionObserver();
		if (obs != null) {
			try {
				obs.onActionFinished(action);
			} catch (final Exception ex) {
				LOGGER.error("Error while notifying Observer" + obs, ex);
			}
		}
	}

	private void dispose() {
		running = false;
		BadgerAction noop = new BadgerAction(null) {
			@Override
			public void runDiskAction(VFSDiskManager diskManager) throws VFSException {

			}
		};

		enqueue(noop);
		workLoadIndicator.dispose();
	}
}
