package ch.eth.jcd.badgers.vfs.core.interfaces;

import ch.eth.jcd.badgers.vfs.exception.VFSException;

/**
 * $Id
 * 
 * Path to a {@link VFSEntry} contrary to {@link VFSEntry} a {@link VFSPath} does not need to exist on the file system
 * 
 * 
 */
public interface VFSPath {

	public VFSEntry createDirectory() throws VFSException;

	public VFSEntry createFile() throws VFSException;

	public boolean exists() throws VFSException;

	public VFSEntry getVFSEntry() throws VFSException;
}
