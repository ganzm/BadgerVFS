package ch.eth.jcd.badgers.vfs.sync.client;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.remote.RemoteAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DiskWorkerController;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.WorkerController;

public class RemoteWorkerController extends WorkerController {
	private static final Logger LOGGER = Logger.getLogger(DiskWorkerController.class);

	public RemoteWorkerController() {
	}

	@Override
	protected void performAction(AbstractBadgerAction abstractAction) {
		RemoteAction action = (RemoteAction) abstractAction;
		try {
			LOGGER.info("Perform Action " + action);
			action.runRemoteAction();
			LOGGER.info("Finished Action " + action);
			actionFinished(action);
		} catch (final VFSException | RuntimeException e) {
			LOGGER.error("", e);
			actionFailed(action, e);
		}
	}
}
