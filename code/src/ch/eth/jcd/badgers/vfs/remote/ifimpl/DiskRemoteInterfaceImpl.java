package ch.eth.jcd.badgers.vfs.remote.ifimpl;

import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.journaling.ClientVersion;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.DiskRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.DiskRemoteResult;
import ch.eth.jcd.badgers.vfs.remote.model.PushVersionResult;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.GetServerVersionAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.GetVersionDeltaAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DiskWorkerController;

public class DiskRemoteInterfaceImpl implements DiskRemoteInterface {

	private static final Logger LOGGER = Logger.getLogger(DiskRemoteInterfaceImpl.class);

	/**
	 * This Instance of DiskWokerController is shared between multiple connections
	 */
	private final DiskWorkerController diskWorkerController;

	/**
	 * In order to achieve single threaded access to a disk we need to lock disk access while a client pulls data to download
	 * 
	 * This Action blocks any other access to a single Disk
	 * 
	 * @see {@link #getVersionDelta(long, long)}
	 * @see {@link #downloadFinished()}
	 */
	private GetVersionDeltaAction getVersionDeltaAction = null;

	private final UUID id;

	public DiskRemoteInterfaceImpl(final DiskWorkerController diskWorkerController) throws VFSException {
		this.diskWorkerController = diskWorkerController;
		diskWorkerController.startWorkerController();
		this.id = UUID.randomUUID();
	}

	@Override
	public DiskRemoteResult longTermPollVersion(final long clientVersion, long timeout) throws RemoteException {
		LOGGER.info("Start Long term Poll for " + id);
		synchronized (diskWorkerController) {
			try {
				diskWorkerController.wait(timeout);
			} catch (InterruptedException e) {
				LOGGER.error("LongTermPoll Interrupted", e);
			}
		}

		try {

			GetServerVersionAction action = new GetServerVersionAction();
			diskWorkerController.enqueueBlocking(action, true);
			DiskRemoteResult result = new DiskRemoteResult(action.getServerVersion());
			return result;
		} catch (VFSException | InterruptedException e) {
			throw new RemoteException("", e);
		} finally {
			LOGGER.info("Long term Poll returned for " + id);
		}
	}

	private void wakeupLongTermPolls() {
		synchronized (diskWorkerController) {
			diskWorkerController.notifyAll();
		}
	}

	@Override
	public List<Journal> getVersionDelta(final long lastSeenServerVersion) throws RemoteException {
		if (getVersionDeltaAction != null) {
			throw new RemoteException("DownloadStreams openend - Someone forgot to call downloadFinished");
		}

		getVersionDeltaAction = new GetVersionDeltaAction(lastSeenServerVersion);
		diskWorkerController.enqueue(getVersionDeltaAction);

		// this call blocks until GetVersionDeltaAction has prepared the result
		try {
			List<Journal> result = getVersionDeltaAction.blockingGetResult();
			return result;
		} catch (InterruptedException e) {
			throw new RemoteException("Interrupted", e);
		}
	}

	@Override
	public void downloadFinished() {
		getVersionDeltaAction.stopBlocking();
		getVersionDeltaAction = null;
	}

	@Override
	public PushVersionResult pushVersion(ClientVersion clientVersion) throws RemoteException {
		try {
			final SyncServerPushVersionAction pushVersionAction = new SyncServerPushVersionAction(clientVersion);
			diskWorkerController.enqueueBlocking(pushVersionAction, true);
			PushVersionResult result = pushVersionAction.getResult();

			wakeupLongTermPolls();

			return result;
		} catch (final InterruptedException | VFSException e) {
			throw new RemoteException("Error pushVersion: ", e);
		}
	}

	@Override
	public void close() throws RemoteException, VFSException {
		diskWorkerController.dispose();
	}

	@Override
	public void unlink() throws RemoteException {
		LOGGER.info("unlink() - TODO");
	}

	@Override
	public UUID getId() throws RemoteException {
		return id;
	}
}
