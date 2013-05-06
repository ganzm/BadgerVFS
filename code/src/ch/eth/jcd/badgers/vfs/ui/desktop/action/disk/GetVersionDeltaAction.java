package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.core.journaling.VFSJournaling;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

/**
 * This DiskAction is performed on the SynchronisationServer. Triggered by a client which wants to update to the latest version.
 */
public class GetVersionDeltaAction extends DiskAction {

	private static final Logger LOGGER = Logger.getLogger(GetVersionDeltaAction.class);

	private final long lastSeenServerVersion;

	private List<Journal> result;

	private Object waitForResultLock = new Object();
	private Object waitForDownloadLock = new Object();
	private boolean waitForResultBool = false;
	private boolean waitForDownloadBool = false;

	public GetVersionDeltaAction(final long lastSeenServerVersion) {
		super(null);
		this.lastSeenServerVersion = lastSeenServerVersion;
	}

	@Override
	public void runDiskAction(final VFSDiskManager diskManager) throws VFSException {
		try {
			VFSJournaling serverJournaling = diskManager.getJournaling();
			List<Journal> journals = serverJournaling.getJournalsSince(lastSeenServerVersion);

			LOGGER.info("Prepare Downloads");
			for (Journal journal : journals) {
				journal.beforeRmiTransport(diskManager);
			}

			setResult(journals);

			// block here
			LOGGER.info("Start blocking");
			synchronized (waitForDownloadLock) {
				if (!waitForDownloadBool) {

					waitForDownloadLock.wait();
				}
			}
			LOGGER.info("End blocking");

		} catch (InterruptedException e) {
			throw new VFSException(e);
		} finally {

			LOGGER.info("Close Downloads");
			for (Journal journal : result) {
				journal.afterRmiTransport(diskManager);
			}
			LOGGER.info("Block DiskManager Finished  " + diskManager);

			// be sure to wake up callers of blockingGetResult, even if there was an error
			setResult(result);
		}
	}

	private void setResult(List<Journal> journals) {
		synchronized (waitForResultLock) {
			result = journals;
			waitForResultBool = true;
			waitForResultLock.notifyAll();
		}
	}

	public void stopBlocking() {
		synchronized (waitForDownloadLock) {
			waitForDownloadBool = true;
			waitForDownloadLock.notifyAll();
		}
	}

	public List<Journal> blockingGetResult() throws InterruptedException {
		synchronized (waitForResultLock) {
			if (!waitForResultBool) {
				waitForResultLock.wait();
			}
		}

		return result;
	}
}
