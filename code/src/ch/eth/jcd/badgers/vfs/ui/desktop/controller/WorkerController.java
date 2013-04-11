package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.BadgerAction;

/**
 * This class handles access to the VFSDiskManager
 * 
 * 
 * 
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
	private ConcurrentLinkedQueue<BadgerAction> actionQueue = new ConcurrentLinkedQueue<>();

	private Thread controllerThread = null;

	private final Object monitorObject = new Object();

	/**
	 * indicates whether the worker is doing anything
	 */
	private boolean busy;

	private boolean running = false;

	public WorkerController(VFSDiskManager diskManager) {
		this.diskManager = diskManager;
	}

	public static void setupWorker(VFSDiskManager diskManager) {
		if (instance != null) {
			throw new VFSRuntimeException("WorkerController already instantiated");
		}
		instance = new WorkerController(diskManager);

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
		synchronized (monitorObject) {
			actionQueue.offer(action);
			monitorObject.notify();
		}
	}

	@Override
	public void run() {
		try {
			running = true;
			BadgerAction action = null;

			busy = true;
			while (running) {

				synchronized (monitorObject) {
					action = actionQueue.poll();
					if (action == null) {
						try {
							busy = false;
							// queue is empty there is no work
							monitorObject.wait();
							busy = true;
						} catch (InterruptedException e) {
							// not too bad - ignore me
							LOGGER.info("WorkerController was interrupted " + e.getMessage());
						}
					}
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
		} finally {
			LOGGER.info("Finished Action " + action);
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
		running = false;
		// wake me up
		monitorObject.notify();
	}
}
