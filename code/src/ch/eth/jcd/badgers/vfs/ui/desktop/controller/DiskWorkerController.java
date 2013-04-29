package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.DiskAction;

public class DiskWorkerController extends WorkerController {
	private static final Logger LOGGER = Logger.getLogger(DiskWorkerController.class);
	private final VFSDiskManager diskManager;
	private final WorkLoadIndicator workLoadIndicator;

	public DiskWorkerController(final VFSDiskManager diskManager) {
		this.diskManager = diskManager;
		this.workLoadIndicator = new WorkLoadIndicator();
	}

	public void enqueue(final DiskAction action) {
		workLoadIndicator.jobEnqueued(action);
		super.enqueue(action);
	}

	public void enqueueBlocking(final DiskAction action) throws InterruptedException {
		synchronized (action) {
			enqueue(action);
			action.wait();
		}
	}

	public VFSDiskManager getDiskManager() {
		return diskManager;
	}

	@Override
	protected void performAction(final AbstractBadgerAction abstractAction) {
		final DiskAction action = (DiskAction) abstractAction;
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
		} finally {
			synchronized (abstractAction) {
				abstractAction.notifyAll();
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		workLoadIndicator.dispose();
	}

	@Override
	protected void workerControllerDisposed() {
		try {
			diskManager.close();
		} catch (final VFSException e) {
			LOGGER.error("Error while closing DiskManager " + diskManager, e);
		}
	}
}
