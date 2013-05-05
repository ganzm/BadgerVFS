package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class ProvideDownloadStreamAction extends DiskAction {

	public ProvideDownloadStreamAction() {
		super(null);
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");

	}

	/**
	 * blocks the calling thread until all RemoteStreams are prepared
	 * 
	 * 
	 */
	public void waitUntilPrepared() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	public void closeStreams() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");

	}

}
