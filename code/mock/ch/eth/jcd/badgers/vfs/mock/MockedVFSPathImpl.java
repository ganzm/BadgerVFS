package ch.eth.jcd.badgers.vfs.mock;

import java.io.File;
import java.nio.file.Files;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class MockedVFSPathImpl implements VFSPath {

	private final String path;
	private final String pathToRoot;

	public MockedVFSPathImpl(String path, String pathToRoot) {
		this.path = path;
		this.pathToRoot = pathToRoot;
	}

	@Override
	public VFSEntry createDirectory() {
		MockedVFSEntryImpl entry = new MockedVFSEntryImpl(path, pathToRoot);
		if (entry.createDirectory()) {
			return entry;
		}
		return null;
	}

	@Override
	public VFSEntry createFile() {
		MockedVFSEntryImpl entry = new MockedVFSEntryImpl(path, pathToRoot);
		if (entry.createFile()) {
			return entry;
		}
		return null;
	}

	@Override
	public boolean exists() {
		MockedVFSEntryImpl entry = new MockedVFSEntryImpl(path, pathToRoot);
		return Files.exists(entry.fileEntry);
	}

	@Override
	public String getAbsolutePath() {
		return VFSPath.FILE_SEPARATOR + path;
	}

	@Override
	public VFSEntry getVFSEntry() {
		return new MockedVFSEntryImpl(path, pathToRoot);
	}

	@Override
	public String getName() throws VFSException {
		String pathString = getAbsolutePath();
		return pathString.substring(pathString.lastIndexOf(File.separatorChar) + 1);
	}

	@Override
	public String getParentPath() throws VFSException {
		String parentPath = getAbsolutePath().substring(0, getAbsolutePath().lastIndexOf(VFSPath.FILE_SEPARATOR));
		if ("".equals(parentPath)) {
			// found the root path
			return VFSPath.FILE_SEPARATOR;
		}
		return parentPath;
	}

}
