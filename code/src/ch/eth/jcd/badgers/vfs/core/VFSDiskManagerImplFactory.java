package ch.eth.jcd.badgers.vfs.core;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class VFSDiskManagerImplFactory extends VFSDiskManagerFactory {

	@Override
	public VFSDiskManager createDiskManager(DiskConfiguration config) throws VFSException {
		return VFSDiskManagerImpl.create(config);
	}

	@Override
	public VFSDiskManager openDiskManager(DiskConfiguration config) throws VFSException {
		return VFSDiskManagerImpl.open(config);
	}

}
