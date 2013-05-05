package ch.eth.jcd.badgers.vfs.core.interfaces;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImplFactory;
import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public abstract class VFSDiskManagerFactory {

	public static VFSDiskManagerFactory getInstance() {
		return new VFSDiskManagerImplFactory();
	}

	public abstract VFSDiskManager createDiskManager(DiskConfiguration config) throws VFSException;

	public abstract VFSDiskManager openDiskManager(DiskConfiguration config) throws VFSException;

}
