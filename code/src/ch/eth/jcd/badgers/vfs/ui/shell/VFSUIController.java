/**
 * JCD Virtual File System 
 * spring 2013
 * Group: Badgers
 * $Id$
 */
package ch.eth.jcd.badgers.vfs.ui.shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.FindInFolderCallback;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.exception.VFSOutOfMemoryException;
import ch.eth.jcd.badgers.vfs.util.ChannelUtil;

public class VFSUIController {

	private static final String NO_CORRECT_NUMBER_OF_PARAMS = "no correct number of parameters for %s command given: expected %s, got %s";

	private static final Logger LOGGER = Logger.getLogger(VFSUIController.class);

	private static final String NO_DISK_OPEN_ERROR = "No disk open, please use open or create command first";
	private static final char[] UNITS = { 'B', 'K', 'M', 'G', 'T', 'P' };

	private final VFSConsole console;

	// current state of the console
	private VFSEntry currentDirectory;
	private VFSDiskManager currentManager;

	public VFSUIController(VFSConsole vfsConsole) {
		this.console = vfsConsole;

	}

	public Command getChangeDirectoryCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("cd command entered");

				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}

				if (param == null || param.length != 1) {
					String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "cd", 1, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);
					console.printHelpMessage();
					return;
				}

				if ("..".equals(param[0])) {
					try {
						currentDirectory = currentDirectory.getParent();
					} catch (VFSException e) {
						LOGGER.error("could not cd to ..", e);
					}
					return;
				}
				VFSEntry childToCD = null;
				try {
					for (VFSEntry child : currentDirectory.getChildren()) {
						if (child.getPath().getAbsolutePath().endsWith(param[0])) {
							childToCD = child;
						}
					}
					if (childToCD == null) {
						String warning = String.format("Child: %s not found  in current directory", param[0]);
						LOGGER.warn(warning);
						console.writeLn(warning);
						return;
					}
					if (!childToCD.isDirectory()) {
						String warning = "Cannot cd to file!";
						LOGGER.warn(warning);
						console.writeLn(warning);
						return;
					}
					currentDirectory = childToCD;

				} catch (VFSException e) {
					LOGGER.error("Could not cd to file:" + param[0], e);
				}

				LOGGER.debug("cd command leaving");

			}
		};
	}

	public Command getCloseCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("close command entered");
				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}

				try {
					currentManager.close();
				} catch (VFSException e) {
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
			public void execute(String[] param) {
				LOGGER.debug("cp command entered");

				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}

				if (param == null || param.length != 2) {
					String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "cp", 2, param == null ? 0 : param.length);
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
				} catch (VFSException e) {
					LOGGER.error("Error copying from " + param[0] + " to " + param[1]);
				}
				LOGGER.debug("cp command leaving");
			}
		};
	}

	public Command getCreateCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("create command entered");
				if (param.length != 2) {
					String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "create", 2, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);
					console.printHelpMessage();
					return;
				}
				try {

					DiskConfiguration config = new DiskConfiguration();
					config.setHostFilePath(param[0]);
					long maximumSizeInMb = Long.parseLong(param[1]);
					config.setMaximumSize(maximumSizeInMb * 1024 * 1024);

					currentManager = VFSDiskManagerFactory.getInstance().createDiskManager(config);
					currentDirectory = currentManager.getRoot();
					console.setPromptString(param[0] + ">");
				} catch (VFSException e) {
					LOGGER.error("Exception while setting up Disk:", e);
				}
				LOGGER.debug("create command leaving");

			}

		};
	}

	public Command getDisposeCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("dispose command entered");
				if (currentManager == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}
				try {
					currentManager.dispose();
				} catch (VFSException e) {
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
			public void execute(String[] param) {
				LOGGER.debug("df command entered");
				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}
				try {
					console.writeLn("VirtualFileSystem\tSize \tUsed \tAvail \tUse% \tMounted on");
					long freeSpace = currentManager.getFreeSpace();
					long maxSpace = currentManager.getMaxSpace();
					console.writeLn(getCurrentVFSPathString() + getFormattedSize(maxSpace) + "\t" + getFormattedSize(maxSpace - freeSpace) + "\t"
							+ getFormattedSize(freeSpace) + "\t" + (int) (((maxSpace - freeSpace) * 100) / maxSpace) + "%\t"
							+ currentManager.getDiskConfiguration().getHostFilePath());
				} catch (VFSException e) {
					LOGGER.error("Error while listing files", e);
				}
				LOGGER.debug("df command leaving");
			}

			private String getCurrentVFSPathString() throws VFSException {
				StringBuilder path = new StringBuilder(currentDirectory.getPath().getAbsolutePath());
				int tabsToAdd = (23 - path.length()) / 8 + 1;
				for (int i = 0; i < tabsToAdd; i++) {
					path.append('\t');
				}
				return path.toString();
			}

			private String getFormattedSize(long size) throws VFSException {
				double tmpSize = size;
				int unit = 0;
				while (tmpSize > 1024) {
					tmpSize = tmpSize / 1024;
					unit++;
				}
				DecimalFormat df = new DecimalFormat("####.#");

				return df.format(tmpSize) + (unit < UNITS.length ? UNITS[unit] : "XL");
			}

		};
	}

	public Command getExitCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
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
			public void execute(String[] param) {
				LOGGER.debug("export command entered");

				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}

				if (param == null || param.length != 2) {
					String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "export", 2, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);
					console.printHelpMessage();
					return;
				}
				File exportFile = new File(param[1]);
				if (exportFile.exists()) {
					LOGGER.warn("export file already exists! I do not overwrite already existing files");
					return;
				}

				try {
					VFSEntry childToExport = null;
					for (VFSEntry child : currentDirectory.getChildren()) {
						if (child.getPath().getAbsolutePath().endsWith(param[0])) {
							childToExport = child;
						}
					}
					if (childToExport == null) {
						String warning = String.format("Child: %s not found in current directory, nothing exported", param[0]);
						LOGGER.warn(warning);
						console.writeLn(warning);

						return;
					}
					InputStream is = childToExport.getInputStream();
					OutputStream os = new FileOutputStream(param[1]);

					ChannelUtil.fastStreamCopy(is, os);
				} catch (VFSException | IOException e) {
					LOGGER.error("Error while exporting file: ", e);
				}
				LOGGER.debug("export command leaving");

			}
		};
	}

	public Command getFindCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {

				LOGGER.debug("find command entered");

				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}

				if (param == null || param.length != 1) {
					String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "find", 1, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);
					console.printHelpMessage();
					return;
				}
				try {
					currentDirectory.findInFolder(param[0], new FindInFolderCallback() {

						@Override
						public void foundEntry(VFSPath path) {
							console.writeLn(path.getAbsolutePath());
						}

						@Override
						public boolean stopSearch(VFSPath currentDirectory) {
							LOGGER.debug("currently looking in:" + currentDirectory.getAbsolutePath());
							// do not stop search
							return false;
						}
					});
				} catch (VFSException e) {
					LOGGER.error("error while find:", e);
				}

				LOGGER.debug("find command leaving");
			}
		};
	}

	public Command getImportCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("import command entered");
				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}

				if (param == null || param.length != 2) {
					String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "import", 2, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);
					console.printHelpMessage();
					return;
				}

				File importFile = new File(param[0]);
				if (!importFile.exists()) {
					String warning = String.format("file to import does not exist: %s", param[0]);
					LOGGER.warn(warning);
					console.writeLn(warning);
					return;
				}

				VFSPath path = null;
				VFSEntry newFile = null;
				try {
					path = currentDirectory.getChildPath(param[1]);
					newFile = path.createFile();

					FileInputStream fis = new FileInputStream(param[0]);
					OutputStream os = newFile.getOutputStream(0);

					ChannelUtil.fastStreamCopy(fis, os);
				} catch (VFSOutOfMemoryException e) {
					if (newFile != null) {
						try {
							LOGGER.debug("deleting partially created File at " + path.getAbsolutePath());
							newFile.delete();
						} catch (VFSException ex) {
							LOGGER.error("internal error while deleting partially created file", ex);
						}
					}
				} catch (IOException | VFSException e) {
					LOGGER.error("Error while importing file: ", e);
				}

				LOGGER.debug("import command leaving");

			}
		};
	}

	public Command getListCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("ls command entered");
				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}
				try {
					for (VFSEntry child : currentDirectory.getChildren()) {
						console.writeLn(child.getPath().getName());
					}
				} catch (VFSException e) {
					LOGGER.error("Error while listing files", e);
				}
				LOGGER.debug("ls command leaving");

			}
		};
	}

	public Command getMakeDirCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("makedir command entered");
				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}
				if (param == null || param.length != 1) {
					String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "mkdir", 1, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);
					console.printHelpMessage();
					return;
				}
				try {
					currentDirectory.getChildPath(param[0]).createDirectory();
				} catch (VFSException e) {
					LOGGER.error("Error while creating new directory:", e);
				}
				LOGGER.debug("makedir command leaving");

			}
		};
	}

	public Command getMakeFileCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("makefile command entered");
				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}
				if (param == null || param.length != 1) {
					String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "mkfile", 1, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);

					console.printHelpMessage();
					return;
				}
				try {
					currentDirectory.getChildPath(param[0]).createFile();
				} catch (VFSException e) {
					LOGGER.error("Error while creating new directory:", e);
				}
				LOGGER.debug("makefile command leaving");

			}
		};
	}

	public Command getMoveCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("mv command entered");

				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}

				if (param == null || param.length != 2) {
					String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "mv", 2, param == null ? 0 : param.length);
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
					srcPath.getVFSEntry().moveTo(dstPath);
				} catch (VFSException e) {
					LOGGER.error("Error moving from " + param[0] + " to " + param[1]);
				}
				LOGGER.debug("mv command leaving");

			}
		};
	}

	public Command getOpenCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("open command entered");

				if (param == null || param.length != 1) {
					String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "create", 1, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);
					console.printHelpMessage();
					return;
				}
				try {
					// create configuration
					DiskConfiguration config = new DiskConfiguration();
					config.setHostFilePath(param[0]);

					currentManager = VFSDiskManagerFactory.getInstance().openDiskManager(config);
					currentDirectory = currentManager.getRoot();
					console.setPromptString(param[0] + ">");
				} catch (VFSException e) {
					LOGGER.error("Exception while setting up Disk:", e);
				}
				LOGGER.debug("open command leaving");

			}
		};
	}

	public Command getPWDCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
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
			public void execute(String[] param) {
				LOGGER.debug("remove command entered");

				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn(NO_DISK_OPEN_ERROR);
					console.writeLn(NO_DISK_OPEN_ERROR);
					return;
				}

				if (param == null || param.length != 1) {
					String logString = String.format(NO_CORRECT_NUMBER_OF_PARAMS, "remove", 1, param == null ? 0 : param.length);
					LOGGER.warn(logString);
					console.writeLn(logString);
					console.printHelpMessage();
					return;
				}
				VFSEntry childToRemove = null;
				try {
					for (VFSEntry child : currentDirectory.getChildren()) {
						if (child.getPath().getAbsolutePath().endsWith(param[0])) {
							childToRemove = child;
						}
					}
					if (childToRemove == null) {
						String warning = String.format("Child: %s not found in current directory, nothing removed", param[0]);
						LOGGER.warn(warning);
						console.writeLn(warning);
						return;
					}

					childToRemove.delete();
				} catch (VFSException e) {
					LOGGER.error("Could not remove file:" + param[0], e);
				}

				LOGGER.debug("remove command leaving");

			}
		};
	}
}
