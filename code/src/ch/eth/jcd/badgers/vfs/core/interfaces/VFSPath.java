package ch.eth.jcd.badgers.vfs.core.interfaces;

public interface VFSPath {

	public VFSEntry createDirectory();

	public VFSEntry createFile();

	public boolean exists();

	public VFSEntry getVFSEntry();
}
