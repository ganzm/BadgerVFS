package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

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

	public boolean isRunning() {
		return running;
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

	protected void enqueueBlocking(final AbstractBadgerAction action, final boolean rethrowException) throws InterruptedException, VFSException {
		synchronized (action) {
			enqueue(action);
			action.wait();
		}

		if (rethrowException) {
			Exception ex = action.getException();
			if (ex != null) {
				// rethrow exception
				throw new VFSException(ex);
			}
		}
	}

	@Override
	public void run() {
		try {
			LOGGER.debug("Starting WorkerController Thread");
			running = true;
			AbstractBadgerAction action = null;

			while (running) {
				long startTime = 0;
				try {
					action = actionQueue.take();
					startTime = System.currentTimeMillis();
					performAction(action);
				} catch (final Exception ex) {
					LOGGER.error("Error while performing Action " + action, ex);
					action.setFailed(ex);
				} finally {
					LOGGER.debug("[" + action.getClass().getSimpleName() + "] performed in " + (System.currentTimeMillis() - startTime) + "ms");
					synchronized (action) {
						action.notifyAll();
					}
				}
			}
		} finally {
			LOGGER.debug("Shutdown WorkerController Thread");
			workerControllerDisposed();
		}
	}

	protected void workerControllerDisposed() {
		LOGGER.info("do nothing in workerControllerDisposed");
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
		LOGGER.info("start dispose WorkerController");
		// do all undone work first.
		try {
			while (actionQueue.size() > 0) {
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			LOGGER.warn(e);
		}
		running = false;

		AbstractBadgerAction noop = createNoopAction();

		enqueue(noop);
		LOGGER.info("done dispose WorkerController");
	}

	/**
	 * Noop Action is used on shutdown to wakeup the thread checking the queue for input
	 * 
	 * @return
	 */
	protected abstract AbstractBadgerAction createNoopAction();
}
