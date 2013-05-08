package ch.eth.jcd.badgers.vfs.sync.client;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.remote.interfaces.DiskRemoteInterface;
import ch.eth.jcd.badgers.vfs.remote.model.DiskRemoteResult;

/**
 * Thread which keeps calling {@link DiskRemoteInterface#longTermPollVersion(long, long)}
 * 
 * @link {@link DiskRemoteInterface#longTermPollVersion(long, long)}
 * 
 */
public class RemoteLongTermPoller implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(RemoteLongTermPoller.class);

	private DiskRemoteInterface diskRemoteInterface;

	private Thread pollThread;

	private boolean running = false;
	private ServerVersionChangedListener listener;

	public RemoteLongTermPoller() {

	}

	public void setServerVersionChangedListener(ServerVersionChangedListener listener) {
		this.listener = listener;
	}

	public void startLongtermPoll(DiskRemoteInterface diskRemoteInterface) {
		LOGGER.info("startLongtermPoll");

		this.diskRemoteInterface = diskRemoteInterface;
		pollThread = new Thread(this);
		pollThread.setDaemon(true);
		pollThread.setName("Poll-" + diskRemoteInterface);

		pollThread.start();

	}

	public void dispose() {
		running = false;

		if (pollThread != null) {
			pollThread.interrupt();
		}
	}

	@Override
	public void run() {
		long timeout = 10000;

		running = true;
		while (running) {
			try {
				DiskRemoteResult result = diskRemoteInterface.longTermPollVersion(timeout);
				listener.serverVersionChanged(result);
			} catch (RemoteException e) {
				LOGGER.error("Error while long term polling", e);
			}
		}
	}
}
