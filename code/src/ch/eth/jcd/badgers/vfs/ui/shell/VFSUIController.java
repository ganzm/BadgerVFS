/**
 * JCD Virtual File System 
 * spring 2013
 * Group: Badgers
 * $Id$
 */
package ch.eth.jcd.badgers.vfs.ui.shell;

import java.io.File;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.VFSExporter;
import ch.eth.jcd.badgers.vfs.core.VFSImporter;
import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.FindInFolderCallback;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.core.model.Compression;
import ch.eth.jcd.badgers.vfs.core.model.DiskSpaceUsage;
import ch.eth.jcd.badgers.vfs.core.model.Encryption;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.exception.VFSInvalidPathException;

public class VFSUIController {

	public static final String NO_CORRECT_NUMBER_OF_PARAMS = "no correct number of parameters for %s command given: expected %s, got %s";

	private static final Logger LOGGER = Logger.getLogger(VFSUIController.class);

	public static final String NO_DISK_OPEN_ERROR = "No disk open, please use open or create command first";
	private static final char[] UNITS = { 'B', 'K', 'M', 'G', 'T', 'P' };

	private final VFSConsole console;

	// current state of the console
	private VFSEntry currentDirectory;
	private VFSDiskManager currentManager;

	public VFSUIController(final VFSConsole vfsConsole) {
		this.console = vfsConsole;

	}

	public Command getChangeDirectoryCommand() {
		return new Command() {

			@Override
			public void execute(final String[] param) {
				LOGGER.debug("cd command entered");

				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}

				if (param == null || param.length != 1) {
					final String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "cd", 1, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);
					console.printHelpMessage();
					return;
				}

				if ("..".equals(param[0])) {
					try {
						currentDirectory = currentDirectory.getParent();
					} catch (final VFSException e) {
						LOGGER.error("could not cd to ..", e);
					}
					return;
				}
				VFSEntry childToCD = null;
				try {
					for (final VFSEntry child : currentDirectory.getChildren()) {
						if (child.getPath().getAbsolutePath().endsWith(param[0])) {
							childToCD = child;
						}
					}
					if (childToCD == null) {
						final String warning = String.format("Child: %s not found in current directory", param[0]);
						LOGGER.warn(warning);
						console.writeLn(warning);
						return;
					}
					if (!childToCD.isDirectory()) {
						final String warning = "Cannot cd to file!";
						LOGGER.warn(warning);
						console.writeLn(warning);
						return;
					}
					currentDirectory = childToCD;

				} catch (final VFSException e) {
					LOGGER.error("Could not cd to file:" + param[0], e);
				}

				LOGGER.debug("cd command leaving");

			}
		};
	}

	public Command getCloseCommand() {
		return new Command() {

			@Override
			public void execute(final String[] param) {
				LOGGER.debug("close command entered");
				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}

				try {
					currentManager.close();
				} catch (final VFSException e) {
					LOGGER.error("Error while closing disk:", e);
				}
				currentManager = null;
				currentDirectory = null;
				console.setPromptString(">");
				LOGGER.debug("close command leaving");

			}
		};
	}

	public Command getCopyCommand() {
		return new Command() {

			@Override
			public void execute(final String[] param) {
				LOGGER.debug("cp command entered");

				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}

				if (param == null || param.length != 2) {
					final String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "cp", 2, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);
					console.printHelpMessage();
					return;
				}
				try {
					VFSPath srcPath;
					if (param[0].startsWith(VFSPath.FILE_SEPARATOR)) {
						srcPath = currentManager.createPath(param[0]);
					} else {
						srcPath = currentDirectory.getChildPath(param[0]);
					}

					VFSPath dstPath;
					if (param[1].startsWith(VFSPath.FILE_SEPARATOR)) {
						dstPath = currentManager.createPath(param[1]);
					} else {
						dstPath = currentDirectory.getChildPath(param[1]);
					}

					srcPath.getVFSEntry().copyTo(dstPath);
				} catch (final VFSException e) {
					LOGGER.error("Error copying from " + param[0] + " to " + param[1]);
				}
				LOGGER.debug("cp command leaving");
			}
		};
	}

	public Command getCreateCommand() {
		return new Command() {

			@Override
			public void execute(final String[] param) {
				LOGGER.debug("create command entered");
				if (param == null || (param.length < 2 && param.length > 4)) {
					final String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "create", 2, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);
					console.printHelpMessage();
					return;
				}
				try {

					final DiskConfiguration config = new DiskConfiguration();
					config.setHostFilePath(param[0]);
					final long maximumSizeInMb = Long.parseLong(param[1]);
					config.setMaximumSize(maximumSizeInMb * 1024 * 1024);
					if (param.length > 2 && param[2] != null) {
						if (Encryption.NONE.name().equalsIgnoreCase(param[2])) {
							config.setEncryptionAlgorithm(Encryption.NONE);
						} else if (Encryption.CAESAR.name().equalsIgnoreCase(param[2])) {
							config.setEncryptionAlgorithm(Encryption.CAESAR);
						}
					}
					if (param.length > 3 && param[3] != null) {
						if (Compression.NONE.name().equalsIgnoreCase(param[3])) {
							config.setCompressionAlgorithm(Compression.NONE);
						} else if (Compression.LZ77.name().equalsIgnoreCase(param[3])) {
							config.setCompressionAlgorithm(Compression.LZ77);
						} else if (Compression.RLE.name().equalsIgnoreCase(param[3])) {
							config.setCompressionAlgorithm(Compression.RLE);
						}
					}

					currentManager = VFSDiskManagerFactory.getInstance().createDiskManager(config);
					currentDirectory = currentManager.getRoot();
					console.setPromptString(param[0] + ">");
				} catch (final VFSException e) {
					LOGGER.error("Exception while setting up Disk:", e);
				}
				LOGGER.debug("create command leaving");

			}

		};
	}

	public Command getDisposeCommand() {
		return new Command() {

			@Override
			public void execute(final String[] param) {
				LOGGER.debug("dispose command entered");
				if (currentManager == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}
				try {
					currentManager.dispose();
				} catch (final VFSException e) {
					LOGGER.error("Error disposing disk:", e);
				}
				currentManager = null;
				currentDirectory = null;
				console.setPromptString(">");
				LOGGER.debug("dispose command leaving");

			}
		};
	}

	public Command getDFCommand() {
		return new Command() {

			@Override
			public void execute(final String[] param) {
				LOGGER.debug("df command entered");
				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}
				try {
					console.writeLn("VirtualFileSystem\tSize \tUsed \tAvail \tUse% \tMounted on");
					final DiskSpaceUsage dm = currentManager.getDiskSpaceUsage();
					final long freeSpace = currentManager.getFreeSpace();
					final long maxSpace = currentManager.getMaxSpace();
					console.writeLn(getCurrentVFSPathString() + getFormattedSize(dm.getMaxData()) + "\t" + getFormattedSize(maxSpace - freeSpace) + "\t"
							+ getFormattedSize(dm.getFreeData()) + "\t" + (int) (((maxSpace - freeSpace) * 100) / maxSpace) + "%\t"
							+ currentManager.getDiskConfiguration().getHostFilePath());
				} catch (final VFSException e) {
					LOGGER.error("Error while listing files", e);
				}
				LOGGER.debug("df command leaving");
			}

			private String getCurrentVFSPathString() throws VFSException {
				final StringBuilder path = new StringBuilder(currentDirectory.getPath().getAbsolutePath());
				final int tabsToAdd = (23 - path.length()) / 8 + 1;
				for (int i = 0; i < tabsToAdd; i++) {
					path.append('\t');
				}
				return path.toString();
			}

			private String getFormattedSize(final long size) throws VFSException {
				double tmpSize = size;
				int unit = 0;
				while (tmpSize > 1024) {
					tmpSize = tmpSize / 1024;
					unit++;
				}
				final DecimalFormat df = new DecimalFormat("####.#");

				return df.format(tmpSize) + (unit < UNITS.length ? UNITS[unit] : "XL");
			}

		};
	}

	public Command getExitCommand() {
		return new Command() {

			@Override
			public void execute(final String[] param) {
				LOGGER.debug("exit command entered");
				if (currentManager != null) {
					console.writeLn("There is still a disk in use, please use close command first");
					return;
				}

				console.stop();

				LOGGER.debug("exit command leaving");
			}
		};
	}

	public Command getExportCommand() {
		return new Command() {

			@Override
			public void execute(final String[] param) {
				LOGGER.debug("export command entered");

				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}

				if (param == null || param.length != 2) {
					final String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "export", 2, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);
					console.printHelpMessage();
					return;
				}
				final File exportFile = new File(param[1]);
				if (exportFile.exists()) {
					LOGGER.warn("export file already exists! I do not overwrite already existing files");
					return;
				}

				try {
					VFSEntry childToExport = null;
					for (final VFSEntry child : currentDirectory.getChildren()) {
						if (child.getPath().getAbsolutePath().endsWith(param[0])) {
							childToExport = child;
						}
					}
					if (childToExport == null) {
						final String warning = String.format("Child: %s not found in current directory, nothing exported", param[0]);
						LOGGER.warn(warning);
						console.writeLn(warning);

						return;
					}
					final List<VFSEntry> entries = new LinkedList<>();
					entries.add(childToExport);
					final VFSExporter exporter = new VFSExporter();
					exporter.exportFileOrFolder(entries, exportFile);
				} catch (final VFSException e) {
					LOGGER.error("Error while exporting file: ", e);
				}
				LOGGER.debug("export command leaving");

			}
		};
	}

	public Command getFindCommand() {
		return new Command() {

			@Override
			public void execute(final String[] param) {

				LOGGER.debug("find command entered");

				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}

				if (param == null || param.length != 1) {
					final String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "find", 1, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);
					console.printHelpMessage();
					return;
				}
				try {
					currentDirectory.findInFolder(param[0], new FindInFolderCallback() {

						@Override
						public void foundEntry(final VFSPath path) {
							console.writeLn(path.getAbsolutePath());
						}

						@Override
						public boolean stopSearch(final VFSPath currentDirectory) {
							LOGGER.debug("currently looking in:" + currentDirectory.getAbsolutePath());
							// do not stop search
							return false;
						}
					});
				} catch (final VFSException e) {
					LOGGER.error("error while find:", e);
				}

				LOGGER.debug("find command leaving");
			}
		};
	}

	public Command getImportCommand() {
		return new Command() {

			@Override
			public void execute(final String[] param) {
				LOGGER.debug("import command entered");
				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}

				if (param == null || param.length != 2) {
					final String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "import", 2, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);
					console.printHelpMessage();
					return;
				}

				try {
					final VFSPath importDestinationPath = currentDirectory.getChildPath(param[1]);
					final VFSImporter importer = new VFSImporter();
					importer.importFileOrFolder(param[0], importDestinationPath);
				} catch (final VFSInvalidPathException ex) {
					LOGGER.info("Invalid User Input for path " + ex.getMessage());
					console.writeLn(ex.getMessage());
				} catch (final VFSException ex) {
					LOGGER.error("Error while importing file: ", ex);
					console.writeLn("There was an error: " + ex.getMessage());
				}

				LOGGER.debug("import command leaving");

			}
		};
	}

	public Command getListCommand() {
		return new Command() {

			@Override
			public void execute(final String[] param) {
				LOGGER.debug("ls command entered");
				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}
				try {
					for (final VFSEntry child : currentDirectory.getChildren()) {
						console.writeLn(child.getPath().getName());
					}
				} catch (final VFSException e) {
					LOGGER.error("Error while listing files", e);
				}
				LOGGER.debug("ls command leaving");

			}
		};
	}

	public Command getMakeDirCommand() {
		return new Command() {

			@Override
			public void execute(final String[] param) {
				LOGGER.debug("makedir command entered");
				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}
				if (param == null || param.length != 1) {
					final String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "mkdir", 1, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);
					console.printHelpMessage();
					return;
				}
				try {
					currentDirectory.getChildPath(param[0]).createDirectory();
				} catch (final VFSException e) {
					LOGGER.error("Error while creating new directory:", e);
				}
				LOGGER.debug("makedir command leaving");

			}
		};
	}

	public Command getMakeFileCommand() {
		return new Command() {

			@Override
			public void execute(final String[] param) {
				LOGGER.debug("makefile command entered");
				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}
				if (param == null || param.length != 1) {
					final String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "mkfile", 1, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);

					console.printHelpMessage();
					return;
				}
				try {
					currentDirectory.getChildPath(param[0]).createFile();
				} catch (final VFSException e) {
					LOGGER.error("Error while creating new directory:", e);
				}
				LOGGER.debug("makefile command leaving");

			}
		};
	}

	public Command getMoveCommand() {
		return new Command() {

			@Override
			public void execute(final String[] param) {
				LOGGER.debug("mv command entered");

				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}

				if (param == null || param.length != 2) {
					final String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "mv", 2, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);
					console.printHelpMessage();
					return;
				}
				try {
					final VFSPath srcPath = param[0].startsWith(VFSPath.FILE_SEPARATOR) ? currentManager.createPath(param[0]) : currentDirectory
							.getChildPath(param[0]);

					final VFSPath dstPath = param[1].startsWith(VFSPath.FILE_SEPARATOR) ? currentManager.createPath(param[1]) : currentDirectory
							.getChildPath(param[1]);

					srcPath.getVFSEntry().moveTo(dstPath);
				} catch (final VFSException e) {
					LOGGER.error("Error moving from " + param[0] + " to " + param[1]);
				}
				LOGGER.debug("mv command leaving");

			}
		};
	}

	public Command getOpenCommand() {
		return new Command() {

			@Override
			public void execute(final String[] param) {
				LOGGER.debug("open command entered");

				if (param == null || param.length != 1) {
					final String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "open", 1, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);
					console.printHelpMessage();
					return;
				}
				try {
					// create configuration
					final DiskConfiguration config = new DiskConfiguration();
					config.setHostFilePath(param[0]);

					currentManager = VFSDiskManagerFactory.getInstance().openDiskManager(config);
					currentDirectory = currentManager.getRoot();
					console.setPromptString(param[0] + ">");
				} catch (final VFSException e) {
					LOGGER.error("Exception while setting up Disk:", e);
				}
				LOGGER.debug("open command leaving");

			}
		};
	}

	public Command getPWDCommand() {
		return new Command() {

			@Override
			public void execute(final String[] param) {
				LOGGER.debug("pwd command entering");
				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}
				console.writeLn(currentDirectory.getPath().getAbsolutePath());

				LOGGER.debug("pwd command leaving");
			}
		};
	}

	public Command getRemoveCommand() {
		return new Command() {

			@Override
			public void execute(final String[] param) {
				LOGGER.debug("remove command entered");

				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}

				if (param == null || param.length != 1) {
					final String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "rm", 1, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);
					console.printHelpMessage();
					return;
				}
				VFSEntry childToRemove = null;
				try {
					for (final VFSEntry child : currentDirectory.getChildren()) {
						if (child.getPath().getAbsolutePath().endsWith(param[0])) {
							childToRemove = child;
						}
					}
					if (childToRemove == null) {
						final String warning = String.format("Child: %s not found in current directory, nothing removed", param[0]);
						LOGGER.warn(warning);
						console.writeLn(warning);
						return;
					}

					childToRemove.delete();
				} catch (final VFSException e) {
					LOGGER.error("Could not remove file:" + param[0], e);
				}

				LOGGER.debug("remove command leaving");

			}
		};
	}
}
