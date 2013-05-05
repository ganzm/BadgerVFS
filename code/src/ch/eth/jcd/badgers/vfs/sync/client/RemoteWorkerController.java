package ch.eth.jcd.badgers.vfs.sync.client;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.remote.RemoteAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.WorkerController;

public class RemoteWorkerController extends WorkerController {
	private static final Logger LOGGER = Logger.getLogger(RemoteWorkerController.class);

	public void enqueue(final RemoteAction action) {
		super.enqueue(action);
	}

	public void enqueueBlocking(final RemoteAction action, final boolean rethrowException) throws InterruptedException, VFSException {
		super.enqueueBlocking(action, rethrowException);
	}

	@Override
	protected void performAction(final AbstractBadgerAction abstractAction) {
		final RemoteAction action = (RemoteAction) abstractAction;
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
