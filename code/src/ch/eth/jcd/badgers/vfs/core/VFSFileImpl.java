package ch.eth.jcd.badgers.vfs.core;

import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;

/**
 * $Id$
 * 
 * 
 * TODO describe VFSFileImpl
 * 
 */
public class VFSFileImpl extends VFSEntryImpl {

	private static Logger LOGGER = Logger.getLogger(VFSDiskManagerImpl.class);

	protected VFSFileImpl(VFSDiskManagerImpl diskManager, VFSPath path) {
		super(diskManager, path);
	}

	@Override
	public List<VFSEntry> getChildren() {
		LOGGER.debug("Tried to call getChildren on File " + getPath());
		return null;
	}

	@Override
	public VFSEntry getChildByName(String fileName) {
		LOGGER.debug("Tried to call getChildByName on File " + getPath());
		return null;
	}

}
