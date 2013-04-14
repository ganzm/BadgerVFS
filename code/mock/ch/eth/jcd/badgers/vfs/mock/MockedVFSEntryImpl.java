package ch.eth.jcd.badgers.vfs.mock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.FindInFolderCallback;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class MockedVFSEntryImpl implements VFSEntry {
	private static final Logger LOGGER = Logger.getLogger(MockedVFSEntryImpl.class);

	protected Path fileEntry;
	protected final String pathToRoot;

	MockedVFSEntryImpl(String path, String pathToRoot) {
		fileEntry = Paths.get(pathToRoot + File.separatorChar + path);
		this.pathToRoot = pathToRoot;
	}

	@Override
	public void copyTo(VFSPath newLocation) {

		try {
			Files.walkFileTree(fileEntry,
					new CopyDirVisitor(fileEntry, Paths.get(pathToRoot + File.separatorChar + newLocation.getAbsolutePath().substring(1))));
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}

	private class CopyDirVisitor extends SimpleFileVisitor<Path> {
		private final Path fromPath;
		private final Path toPath;
		private final StandardCopyOption copyOption = StandardCopyOption.REPLACE_EXISTING;

		public CopyDirVisitor(Path fromPath, Path toPath) {
			this.fromPath = fromPath;
			this.toPath = toPath;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			Path targetPath = toPath.resolve(fromPath.relativize(dir));
			if (!Files.exists(targetPath)) {
				Files.createDirectory(targetPath);
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
			return FileVisitResult.CONTINUE;
		}
	}

	protected boolean createFile() {
		try {
			Files.createFile(fileEntry);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	protected boolean createDirectory() {
		try {
			Files.createDirectory(fileEntry);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	protected String getAbsolutePath() throws IOException {
		return fileEntry.toAbsolutePath().toString();
	}

	@Override
	public List<VFSEntry> getChildren() throws VFSException {
		List<VFSEntry> childs = new LinkedList<VFSEntry>();
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(fileEntry)) {
			for (Path path : directoryStream) {
				if (path.toAbsolutePath().toString().equals(pathToRoot)) {
					childs.add(new MockedVFSEntryImpl(path.toAbsolutePath().toString().substring(pathToRoot.length()), pathToRoot));
				} else {
					childs.add(new MockedVFSEntryImpl(path.toAbsolutePath().toString().substring(pathToRoot.length() + 1), pathToRoot));
				}
			}
		} catch (IOException ex) {
			throw new VFSException(ex);
		}
		return childs;
	}

	@Override
	public InputStream getInputStream() {
		try {
			return Files.newInputStream(fileEntry);
		} catch (IOException e) {
			LOGGER.error("", e);
		}
		return null;
	}

	@Override
	public OutputStream getOutputStream(int writeMode) {
		try {
			return Files.newOutputStream(fileEntry);
		} catch (IOException e) {
			LOGGER.error("", e);
		}
		return null;
	}

	@Override
	public VFSPath getPath() {
		if (fileEntry.toAbsolutePath().toString().equals(pathToRoot)) {
			return new MockedVFSPathImpl(fileEntry.toAbsolutePath().toString().substring(pathToRoot.length()), pathToRoot);
		} else {
			return new MockedVFSPathImpl(fileEntry.toAbsolutePath().toString().substring(pathToRoot.length() + 1), pathToRoot);
		}
	}

	@Override
	public boolean isDirectory() {
		return Files.isDirectory(fileEntry);
	}

	@Override
	public void moveTo(VFSPath path) {
		Path toFile;
		try {
			toFile = Paths.get(((MockedVFSEntryImpl) (path.getVFSEntry())).getAbsolutePath());
			Files.move(fileEntry, toFile);
		} catch (IOException e) {
			LOGGER.error("", e);
		} catch (VFSException e) {
			LOGGER.error("", e);
		}

	}

	@Override
	public void renameTo(String newName) {
		try {
			fileEntry = Files.move(fileEntry, fileEntry.resolveSibling(newName));
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof MockedVFSEntryImpl) {
			MockedVFSEntryImpl obj = (MockedVFSEntryImpl) o;
			if (obj.fileEntry.equals(this)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return fileEntry.hashCode();
	}

	@Override
	public void delete() {
		try {
			Files.delete(fileEntry);
		} catch (IOException e) {
			LOGGER.error("", e);
		}

	}

	@Override
	public VFSPath getChildPath(String childName) {
		if (fileEntry.toAbsolutePath().toString().equals(pathToRoot)) {
			return new MockedVFSPathImpl(childName, pathToRoot);
		} else {
			return new MockedVFSPathImpl(fileEntry.toAbsolutePath().toString().substring(pathToRoot.length() + 1) + File.separatorChar + childName, pathToRoot);
		}
	}

	@Override
	public VFSEntry getParent() {
		if (!fileEntry.toAbsolutePath().toString().equals(pathToRoot)) {
			return new MockedVFSEntryImpl(fileEntry.toAbsolutePath().toString()
					.substring(pathToRoot.length() + 1, fileEntry.toAbsolutePath().toString().lastIndexOf(File.separatorChar) + 1), pathToRoot);
		}
		return null;
	}

	@Override
	public void findInFolder(String fileName, FindInFolderCallback observer) throws VFSException {
		if (!this.isDirectory()) {
			throw new VFSException("this is not a directory, search not allowed");
		}

		try {
			Files.walkFileTree(fileEntry, new FinderVisitor(fileName, observer));
		} catch (IOException e) {
			LOGGER.error("", e);
		}

	}

	private class FinderVisitor extends SimpleFileVisitor<Path> {

		private final PathMatcher matcher;
		private final FindInFolderCallback observer;

		FinderVisitor(String pattern, FindInFolderCallback observer) {
			matcher = FileSystems.getDefault().getPathMatcher("glob:*" + pattern + "*");
			this.observer = observer;
		}

		// Compares the glob pattern against
		// the file or directory name.
		private void find(Path file) {
			Path name = file;
			if (name != null && matcher.matches(name.getFileName()) && !name.getFileName().equals(fileEntry.getFileName())) {
				LOGGER.debug("Found: " + file);
				VFSPath vfsPath = new MockedVFSPathImpl(name.toAbsolutePath().toString().substring(pathToRoot.length() + 1).replace("\\", "/"), pathToRoot);
				observer.foundEntry(vfsPath);
			}
		}

		// Invoke the pattern matching
		// method on each file.
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			if (observer.stopSearch(null)) {
				return FileVisitResult.TERMINATE;
			}
			find(file);
			return FileVisitResult.CONTINUE;
		}

		// Invoke the pattern matching
		// method on each directory.
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
			find(dir);
			return FileVisitResult.CONTINUE;
		}
	}

}
