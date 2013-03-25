package ch.eth.jcd.badgers.vfs.mock;

import java.io.File;
import java.nio.file.Files;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;

public class MockedVFSPath implements VFSPath {

	String path;
	String pathToRoot;

	public MockedVFSPath(String path, String pathToRoot) {
		this.path = path;
		this.pathToRoot = pathToRoot;
	}

	public MockedVFSPath(String path, VFSEntry root) {
		this.path = path;
		this.pathToRoot = ((MockedVFSPath) root.getPath()).getAbsolutPath();
	}

	@Override
	public VFSEntry createDirectory() {
		MockedVFSEntry entry = new MockedVFSEntry(path, pathToRoot);
		if (entry.createVFSEntry()) {
			return entry;
		}
		return null;
	}

	@Override
	public VFSEntry createFile() {
		return new MockedVFSEntry(path, pathToRoot);
	}

	@Override
	public boolean exists() {
		MockedVFSEntry entry = new MockedVFSEntry(path, pathToRoot);
		return Files.exists(entry.fileEntry);
	}

	public String getAbsolutPath() {
		if (path.length() > 0) {
			return pathToRoot + File.separatorChar + path;
		}
		return pathToRoot;
	}

	@Override
	public String getPathString() {
		return path;
	}

	@Override
	public VFSEntry getVFSEntry() {
		return new MockedVFSEntry(path, pathToRoot);
	}

}
