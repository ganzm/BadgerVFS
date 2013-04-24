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

	public void enqueue(DiskAction action) {
		workLoadIndicator.jobEnqueued(action);
		super.enqueue(action);
	}

	@Override
	protected void performAction(final AbstractBadgerAction abstractAction) {
		DiskAction action = (DiskAction) abstractAction;
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

	@Override
	public void dispose() {
		super.dispose();
		workLoadIndicator.dispose();
	}
}
