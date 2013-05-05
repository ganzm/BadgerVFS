package ch.eth.jcd.badgers.vfs.remote.ifimpl;

import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.journaling.ClientVersion;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.DiskRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.DiskRemoteResult;
import ch.eth.jcd.badgers.vfs.remote.model.PushVersionResult;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.ProvideDownloadStreamAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DiskWorkerController;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.GetVersionDeltaAction;

public class DiskRemoteInterfaceImpl implements DiskRemoteInterface {

	private static final Logger LOGGER = Logger.getLogger(DiskRemoteInterfaceImpl.class);

	private final BlockingQueue<DiskRemoteResult> clientRequests = new LinkedBlockingQueue<DiskRemoteResult>();

	private final DiskWorkerController diskWorkerController;

	/**
	 * In order to achieve single thread access to a disk we need to lock disk access while a client pulls data to download
	 * 
	 * This Action blocks any other access to a single Disk
	 * 
	 * @see {@link #getVersionDelta(long, long)}
	 * @see {@link #downloadFinished()}
	 */
	private ProvideDownloadStreamAction downloadAction = null;

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
	public List<Journal> getVersionDelta(final long lastSeenServerVersion) throws RemoteException {
		if (downloadAction != null) {
			throw new RemoteException("DownloadStreams openend - Someone forgot to call downloadFinished");
		}

		try {
			final GetVersionDeltaAction deltaAction = new GetVersionDeltaAction(lastSeenServerVersion);
			diskWorkerController.enqueueBlocking(deltaAction, true);

			List<Journal> result = deltaAction.getResult();
			downloadAction = new ProvideDownloadStreamAction(result);

			diskWorkerController.enqueue(downloadAction);
			downloadAction.waitUntilPrepared();

			return result;
		} catch (final InterruptedException | VFSException e) {
			throw new RemoteException("error getting version Delta", e);
		}
	}

	public void downloadFinished() {
		downloadAction.closeStreams();
		downloadAction = null;
	}

	@Override
	public PushVersionResult pushVersion(ClientVersion clientVersion) throws RemoteException {
		try {
			final SyncServerPushVersionAction pushVersionAction = new SyncServerPushVersionAction(clientVersion);
			diskWorkerController.enqueueBlocking(pushVersionAction, true);
			return pushVersionAction.getResult();
		} catch (final InterruptedException | VFSException e) {
			throw new RemoteException("Error pushVersion: ", e);
		}
	}

	public void close() throws RemoteException, VFSException {
		diskWorkerController.dispose();
	}

	@Override
	public void unlink() throws RemoteException {
		LOGGER.info("unlink() - TODO");
	}
}
