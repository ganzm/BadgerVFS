package ch.eth.jcd.badgers.vfs.mock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class MockedVFSEntry implements VFSEntry {

	Path fileEntry;
	final String pathToRoot;

	MockedVFSEntry(String path, String pathToRoot) {
		fileEntry = Paths.get(pathToRoot + File.separatorChar + path);
		this.pathToRoot = pathToRoot;
	}

	@Override
	public void copyTo(VFSPath newLocation) {

		try {
			Files.walkFileTree(fileEntry, new CopyDirVisitor(fileEntry, Paths.get(pathToRoot + File.separatorChar + newLocation.getPathString().substring(1))));
		} catch (IOException | VFSException e) {
			e.printStackTrace();
		}
		// try {
		// Files.copy(fileEntry, Paths.get(pathToRoot + File.separatorChar + newLocation.getPath()));
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

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

	boolean createFile() {
		try {
			Files.createFile(fileEntry);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return true;
	}

	boolean createDirectory() {
		try {
			Files.createDirectory(fileEntry);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return true;
	}

	String getAbsolutePath() throws IOException {
		return fileEntry.toAbsolutePath().toString();
	}

	@Override
	public List<VFSEntry> getChildren() {
		List<VFSEntry> childs = new LinkedList<VFSEntry>();
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(fileEntry)) {
			for (Path path : directoryStream) {
				if (path.toAbsolutePath().toString().equals(pathToRoot)) {
					childs.add(new MockedVFSEntry(path.toAbsolutePath().toString().substring(pathToRoot.length()), pathToRoot));
				} else {
					childs.add(new MockedVFSEntry(path.toAbsolutePath().toString().substring(pathToRoot.length() + 1), pathToRoot));
				}
			}
		} catch (IOException ex) {
		}
		return childs;
	}

	@Override
	public InputStream getInputStream() {
		try {
			return Files.newInputStream(fileEntry);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public OutputStream getOutputStream(int writeMode) {
		try {
			return Files.newOutputStream(fileEntry);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public VFSPath getPath() {
		if (fileEntry.toAbsolutePath().toString().equals(pathToRoot)) {
			return new MockedVFSPath(fileEntry.toAbsolutePath().toString().substring(pathToRoot.length()), pathToRoot);
		} else {
			return new MockedVFSPath(fileEntry.toAbsolutePath().toString().substring(pathToRoot.length() + 1), pathToRoot);
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
			toFile = Paths.get(((MockedVFSEntry) (path.getVFSEntry())).getAbsolutePath());
			Files.move(fileEntry, toFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (VFSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void renameTo(String newName) {
		try {
			fileEntry = Files.move(fileEntry, fileEntry.resolveSibling(newName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof MockedVFSEntry) {
			MockedVFSEntry obj = (MockedVFSEntry) o;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public VFSPath getNewChildPath(String childName) {
		if (fileEntry.toAbsolutePath().toString().equals(pathToRoot)) {
			return new MockedVFSPath(childName, pathToRoot);
		} else {
			return new MockedVFSPath(fileEntry.toAbsolutePath().toString().substring(pathToRoot.length() + 1) + File.separatorChar + childName, pathToRoot);
		}
	}

	@Override
	public VFSEntry getParent() {
		if (!fileEntry.toAbsolutePath().toString().equals(pathToRoot)) {
			return new MockedVFSEntry(fileEntry.toAbsolutePath().toString()
					.substring(pathToRoot.length() + 1, fileEntry.toAbsolutePath().toString().lastIndexOf(File.separatorChar)), pathToRoot);
		}
		return null;
	}

}
