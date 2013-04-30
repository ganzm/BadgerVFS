package ch.eth.jcd.badgers.vfs.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.FindInFolderCallback;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.journaling.Journal;
import ch.eth.jcd.badgers.vfs.core.model.DiskSpaceUsage;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public final class MockedVFSDiskManagerImpl implements VFSDiskManager {

	private static final Logger LOGGER = Logger.getLogger(MockedVFSDiskManagerImpl.class);

	/**
	 * creates a new virtual disk
	 * 
	 * @param config
	 *            Configuration used
	 * @return
	 */
	public static MockedVFSDiskManagerImpl create(DiskConfiguration config) throws VFSException {
		LOGGER.info("Open Mocked BadgerVFS Disk on " + config.getHostFilePath());
		LOGGER.debug("Using Config " + config.toString());
		MockedVFSDiskManagerImpl mgr = new MockedVFSDiskManagerImpl(config);

		File file = new File(config.getHostFilePath());
		if (!file.exists()) {
			file.mkdir();
		}
		if (!file.isDirectory()) {
			throw new VFSException("Cannot open MockedVFSDiskManager because the path:  " + config.getHostFilePath() + " does not exist or is not a directory");
		}

		return mgr;
	}

	/**
	 * Opens an existing virtual disk and opens is
	 * 
	 * @param config
	 *            Configuration used
	 * @return
	 */
	public static MockedVFSDiskManagerImpl open(DiskConfiguration config) throws VFSException {

		LOGGER.info("Open Mocked BadgerVFS Disk on " + config.getHostFilePath());
		LOGGER.debug("Using Config " + config.toString());
		MockedVFSDiskManagerImpl mgr = new MockedVFSDiskManagerImpl(config);

		File file = new File(config.getHostFilePath());
		if (!file.exists() || !file.isDirectory()) {
			throw new VFSException("Cannot open MockedVFSDiskManager because the path:  " + config.getHostFilePath() + " does not exist or is not a directory");
		}

		return mgr;
	}

	private final DiskConfiguration config;

	private MockedVFSDiskManagerImpl(DiskConfiguration config) {
		this.config = config;
	}

	@Override
	public void close() {
		return;
	}

	@Override
	public void dispose() {
		Path dir = Paths.get(config.getHostFilePath());
		try {
			Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

					LOGGER.debug("Deleting dir: " + dir);
					if (exc == null) {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					} else {
						throw exc;
					}
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

					LOGGER.debug("Deleting file: " + file);
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

			});
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}

	@Override
	public long getFreeSpace() {
		return 1;
	}

	@Override
	public long getMaxSpace() {
		return 0;
	}

	@Override
	public DiskSpaceUsage getDiskSpaceUsage() throws VFSException {
		return new DiskSpaceUsage();
	}

	@Override
	public VFSEntry getRoot() {
		return new MockedVFSPathImpl("", config.getHostFilePath()).getVFSEntry();
	}

	@Override
	public DiskConfiguration getDiskConfiguration() throws VFSException {
		return config;
	}

	@Override
	public VFSPath createPath(String path) throws VFSException {
		return new MockedVFSPathImpl(path.replace(VFSPath.FILE_SEPARATOR.charAt(0), File.separatorChar).substring(1), config.getHostFilePath());
	}

	@Override
	public void find(String fileName, FindInFolderCallback observer) throws VFSException {
		getRoot().findInFolder(fileName, observer);
	}

	@Override
	public void closeCurrentJournal() throws VFSException {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Journal linkDisk(String hostName) throws VFSException {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void pauseJournaling(boolean pause) {
	}

	@Override
	public List<Journal> getPendingJournals() throws VFSException {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public UUID getDiskId() {
		throw new UnsupportedOperationException("TODO");
	}
}
