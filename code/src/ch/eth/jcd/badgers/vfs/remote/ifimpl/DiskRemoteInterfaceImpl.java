package ch.eth.jcd.badgers.vfs.remote.ifimpl;

import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.DiskRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.DiskRemoteResult;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DiskWorkerController;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.GetVersionDeltaAction;

public class DiskRemoteInterfaceImpl implements DiskRemoteInterface {
	private static final Logger LOGGER = Logger.getLogger(DiskRemoteInterfaceImpl.class);

	private static final ActionObserver DUMMY_OBSERVER = new ActionObserver() {

		@Override
		public void onActionFinished(final AbstractBadgerAction action) {

		}

		@Override
		public void onActionFailed(final AbstractBadgerAction action, final Exception e) {

		}
	};

	private final BlockingQueue<DiskRemoteResult> clientRequests = new LinkedBlockingQueue<DiskRemoteResult>();
	private final DiskWorkerController diskWorkerController;

	public DiskRemoteInterfaceImpl(final DiskWorkerController diskWorkerController) throws VFSException {
		this.diskWorkerController = diskWorkerController;
		diskWorkerController.startWorkerController();
	}

	@Override
	public DiskRemoteResult longTermPollVersion(final long clientVersion) throws RemoteException {
		try {
			return clientRequests.take();
		} catch (final InterruptedException e) {
			LOGGER.info("Interrupted on take", e);
		}
		return null;
	}

	public void sendToClient(final DiskRemoteResult drr) {
		clientRequests.offer(drr);
	}

	@Override
	public List<Journal> getVersionDelta(final long lastSeenServerVersion, final long clientVersion) throws RemoteException {
		try {
			final GetVersionDeltaAction deltaAction = new GetVersionDeltaAction(lastSeenServerVersion, clientVersion, DUMMY_OBSERVER);
			diskWorkerController.enqueueBlocking(deltaAction);
			return deltaAction.getResult();
		} catch (final InterruptedException e) {
			throw new RemoteException("error getting version Delta", e);
		}

	}

	@Override
	public Journal pushVersion(final long lastSeenServerVersion, final Journal clientJournal) throws RemoteException {
		try {
			final PushVersionAction pushVersionAction = new PushVersionAction(lastSeenServerVersion, clientJournal, DUMMY_OBSERVER);
			diskWorkerController.enqueueBlocking(pushVersionAction);
			return pushVersionAction.getResult();
		} catch (final InterruptedException e) {
			throw new RemoteException("Error pushVersion: ", e);
		}
	}

	public void close() throws RemoteException, VFSException {
		diskWorkerController.dispose();
	}

	@Override
	public void unlink() throws RemoteException {

		throw new UnsupportedOperationException("TODO");
	}

}
