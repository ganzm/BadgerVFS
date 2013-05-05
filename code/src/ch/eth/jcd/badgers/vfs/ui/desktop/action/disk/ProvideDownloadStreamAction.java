package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class ProvideDownloadStreamAction extends DiskAction {

	private static final Logger LOGGER = Logger.getLogger(ProvideDownloadStreamAction.class);

	private final List<Journal> journals;
	private boolean preparedFlag = false;
	private Object preparedMonitor = new Object();

	private boolean streamsOpenedFlag = false;
	private Object streamOpenMonitor = new Object();

	/**
	 * 
	 */
	private long clientDownloadTimeout = 10 * 60 * 1000;

	public ProvideDownloadStreamAction(List<Journal> journals) {
		super(null);
		this.journals = journals;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		synchronized (streamOpenMonitor) {
			streamsOpenedFlag = true;
		}

		prepareDownloads(diskManager);

		setPrepared();

		try {
			LOGGER.info("Block DiskManager until client download is finished " + diskManager);

			synchronized (streamOpenMonitor) {
				if (streamsOpenedFlag) {
					streamOpenMonitor.wait(clientDownloadTimeout);
				}
			}
		} catch (InterruptedException e) {
			throw new VFSException(e);
		} finally {
			closeDownloads(diskManager);
			LOGGER.info("Block DiskManager Finished  " + diskManager);
		}
	}

	private void closeDownloads(VFSDiskManager diskManager) {
		LOGGER.info("Close Downloads");
		for (Journal journal : journals) {
			journal.afterRmiTransport(diskManager);
		}
	}

	private void prepareDownloads(VFSDiskManager diskManager) throws VFSException {
		LOGGER.info("Prepare Downloads");
		for (Journal journal : journals) {
			journal.beforeRmiTransport(diskManager);
		}
	}

	/**
	 * blocks the calling thread until all RemoteStreams are prepared
	 * 
	 * @throws InterruptedException
	 * 
	 * 
	 */
	public void waitUntilPrepared() throws InterruptedException {
		synchronized (preparedMonitor) {
			if (preparedFlag) {
				return;
			}
			preparedMonitor.wait();
		}
	}

	public void closeStreams() {
		synchronized (streamOpenMonitor) {
			streamsOpenedFlag = true;
			streamOpenMonitor.notifyAll();

		}
	}

	private void setPrepared() {
		synchronized (preparedMonitor) {
			preparedFlag = true;
			preparedMonitor.notify();
		}
	}
}
