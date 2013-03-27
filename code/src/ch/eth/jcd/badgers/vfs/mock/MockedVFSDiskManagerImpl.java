package ch.eth.jcd.badgers.vfs.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class MockedVFSDiskManagerImpl implements VFSDiskManager {

	private static Logger logger = Logger.getLogger(MockedVFSDiskManagerImpl.class);

	/**
	 * creates a new virtual disk
	 * 
	 * @param config
	 *            Configuration used
	 * @return
	 */
	public static MockedVFSDiskManagerImpl create(DiskConfiguration config) throws VFSException {
		try {
			logger.info("Open Mocked BadgerVFS Disk on " + config.getHostFilePath());
			logger.debug("Using Config " + config.toString());
			MockedVFSDiskManagerImpl mgr = new MockedVFSDiskManagerImpl(config);

			File file = new File(config.getHostFilePath());
			if (!file.exists()) {
				file.mkdir();
			}
			if (!file.isDirectory()) {
				throw new VFSException("Cannot open MockedVFSDiskManager because the path:  " + config.getHostFilePath()
						+ " does not exist or is not a directory");
			}

			return mgr;

		} catch (Exception e) {
			throw new VFSException(e);
		}
	}

	/**
	 * Opens an existing virtual disk and opens is
	 * 
	 * @param config
	 *            Configuration used
	 * @return
	 */
	public static MockedVFSDiskManagerImpl open(DiskConfiguration config) throws VFSException {

		try {
			logger.info("Open Mocked BadgerVFS Disk on " + config.getHostFilePath());
			logger.debug("Using Config " + config.toString());
			MockedVFSDiskManagerImpl mgr = new MockedVFSDiskManagerImpl(config);

			File file = new File(config.getHostFilePath());
			if (!file.exists() || !file.isDirectory()) {
				throw new VFSException("Cannot open MockedVFSDiskManager because the path:  " + config.getHostFilePath()
						+ " does not exist or is not a directory");
			}

			return mgr;

		} catch (Exception e) {
			throw new VFSException(e);
		}
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

					System.out.println("Deleting dir: " + dir);
					if (exc == null) {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					} else {
						throw exc;
					}
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

					System.out.println("Deleting file: " + file);
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public long getFreeSpace() {
		return 0;
	}

	@Override
	public VFSEntry getRoot() {
		return new MockedVFSPath("", config.getHostFilePath()).getVFSEntry();
	}

	@Override
	public VFSPath CreatePath(String path) throws VFSException {
		return new MockedVFSPath(path, config.getHostFilePath());
	}

	@Override
	public DiskConfiguration getDiskConfiguration() throws VFSException {
		return config;
	}

}
