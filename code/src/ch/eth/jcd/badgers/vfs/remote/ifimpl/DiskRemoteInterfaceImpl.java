package ch.eth.jcd.badgers.vfs.remote.ifimpl;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.interfaces.DiskRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.DiskRemoteResult;
import ch.eth.jcd.badgers.vfs.remote.model.Journal;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DiskWorkerController;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.GetVersionDeltaAction;

public class DiskRemoteInterfaceImpl implements DiskRemoteInterface {
	private static final Logger LOGGER = Logger.getLogger(DiskRemoteInterfaceImpl.class);

	private final BlockingQueue<DiskRemoteResult> clientRequests = new LinkedBlockingQueue<DiskRemoteResult>();
	private final DiskWorkerController diskWorkerController;

	private final Lock blockingRequestLock = new ReentrantLock();

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
			blockingRequestLock.lock();
			final Condition requestCondition = blockingRequestLock.newCondition();
			final List<Journal> retVal = new LinkedList<Journal>();
			diskWorkerController.enqueue(new GetVersionDeltaAction(lastSeenServerVersion, clientVersion, new ActionObserver() {

				@Override
				public void onActionFinished(final AbstractBadgerAction action) {
					try {
						blockingRequestLock.lock();
						final List<Journal> tmp = ((GetVersionDeltaAction) action).getResult();
						retVal.addAll(tmp);
						requestCondition.signal();
					} finally {
						blockingRequestLock.unlock();
					}

				}

				@Override
				public void onActionFailed(final AbstractBadgerAction action, final Exception e) {
					try {
						blockingRequestLock.lock();
						LOGGER.error("GetVersionDeltaAction failed, returning empty journal list!");
						requestCondition.signal();
					} finally {
						blockingRequestLock.unlock();
					}
				}
			}));
			requestCondition.await();
			return retVal;
		} catch (final InterruptedException e1) {
			LOGGER.info("Interrupted in getVersionDelta", e1);
		} finally {
			blockingRequestLock.unlock();
		}
		return null;
	}

	@Override
	public Journal pushVersion(final long lastSeenServerVersion, final Journal clientJournal) throws RemoteException {
		try {
			blockingRequestLock.lock();
			final Condition requestCondition = blockingRequestLock.newCondition();
			// this is an ugly hack to get a return value from inner class
			final Journal[] retVal = new Journal[1];
			diskWorkerController.enqueue(new PushVersionAction(lastSeenServerVersion, clientJournal, new ActionObserver() {

				@Override
				public void onActionFinished(final AbstractBadgerAction action) {
					try {
						blockingRequestLock.lock();
						final Journal tmp = ((PushVersionAction) action).getResult();
						retVal[0] = tmp;
						requestCondition.signal();
					} finally {
						blockingRequestLock.unlock();
					}

				}

				@Override
				public void onActionFailed(final AbstractBadgerAction action, final Exception e) {
					try {
						blockingRequestLock.lock();
						LOGGER.error("GetVersionDeltaAction failed, returning empty journal list!");
						requestCondition.signal();
					} finally {
						blockingRequestLock.unlock();
					}
				}
			}));
			requestCondition.await();
			return retVal[0];
		} catch (final InterruptedException e1) {
			LOGGER.info("Interrupted in getVersionDelta", e1);
		} finally {
			blockingRequestLock.unlock();
		}
		return null;
	}

	public void close() throws RemoteException, VFSException {
		diskWorkerController.dispose();
	}

	@Override
	public void unlink() throws RemoteException {
		// TODO Auto-generated method stub

	}

}
