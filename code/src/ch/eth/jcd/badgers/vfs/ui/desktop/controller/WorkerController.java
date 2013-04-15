package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

	/**
	 * Queue contains unprocesses jobs
	 */
	private final ConcurrentLinkedQueue<BadgerAction> actionQueue = new ConcurrentLinkedQueue<>();

	private final List<ActionObserver> actionObservers = new ArrayList<ActionObserver>();

	private Thread controllerThread;

	private final Lock lock = new ReentrantLock();
	private final Condition condition = lock.newCondition();

	/**
	 * indicates whether the worker is doing anything
	 */
	private boolean busy;

	private boolean running = false;

	public WorkerController(VFSDiskManager diskManager) {
		this.diskManager = diskManager;
	}

	public static WorkerController setupWorker(VFSDiskManager diskManager) {
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
		controllerThread = new Thread(this);
		controllerThread.setName("WorkerController");
		controllerThread.start();
	}

	public void enqueue(BadgerAction action) {
		try {
			lock.lock();
			actionQueue.offer(action);
			condition.signalAll();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void run() {
		try {
			LOGGER.debug("Starting WorkerController Thread");
			running = true;
			BadgerAction action = null;

			busy = true;
			while (running) {

				try {
					lock.lock();

					action = actionQueue.poll();
					if (action == null) {
						try {
							busy = false;
							// queue is empty there is no work
							condition.await();
							busy = true;
						} catch (InterruptedException e) {
							// not too bad - ignore me
							LOGGER.info("WorkerController was interrupted " + e.getMessage());
						}
					}
				} finally {
					lock.unlock();
				}
				if (action != null) {
					try {
						performAction(action);
					} catch (Exception ex) {
						LOGGER.error("Error while performing Action " + action, ex);
					}
				}
			}
		} finally {
			LOGGER.debug("Shutdown WorkerController Thread");
		}
	}

	private void performAction(BadgerAction action) {
		try {
			LOGGER.info("Perform Action " + action);
			action.runDiskAction(diskManager);
		} catch (VFSException e) {
			LOGGER.error("", e);
			actionFailed(action, e);
		} finally {
			LOGGER.info("Finished Action " + action);
			actionFinished(action);
		}
	}

	public void addActionObserver(ActionObserver actionObserver) {
		actionObservers.add(actionObserver);
	}

	private void actionFailed(BadgerAction action, VFSException e) {
		for (ActionObserver obs : actionObservers) {
			try {
				obs.onActionFailed(action, e);
			} catch (Exception ex) {
				LOGGER.error("Error while notifying Observer " + obs, ex);
			}
		}
	}

	private void actionFinished(BadgerAction action) {
		for (ActionObserver obs : actionObservers) {
			try {
				obs.onActionFinished(action);
			} catch (Exception ex) {
				LOGGER.error("Error while notifying Observer" + obs, ex);
			}
		}
	}

	/**
	 * indicates whether the worker is doing anything
	 * 
	 * @return
	 */
	public boolean isBusy() {
		return busy;
	}

	private void dispose() {
		try {
			lock.lock();
			running = false;
			// wake me up

			condition.signalAll();
		} finally {
			lock.unlock();
		}
	}
}
