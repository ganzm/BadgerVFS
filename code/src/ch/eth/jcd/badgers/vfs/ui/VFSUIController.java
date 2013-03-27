/**
 * JCD Virtual File System 
 * spring 2013
 * Group: Badgers
 * $Id$
 */
package ch.eth.jcd.badgers.vfs.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.mock.MockedVFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.util.ChannelUtil;

/**
 * $Id$ TODO describe VFSUIController
 * 
 */
public class VFSUIController {

	/**
	 * needs to be set when using real implementation
	 */

	public static final Class<? extends VFSDiskManager> DISKMANAGER_IMPLEMENTATION = MockedVFSDiskManagerImpl.class;
	// public static final Class<? extends VFSDiskManager> DISKMANAGER_IMPLEMENTATION = BadgerFSDiskManagerImpl.class;

	private static final Logger LOGGER = Logger.getLogger(VFSUIController.class);

	/**
	 * Instantiates a VFSDiskManager Implementation. This is done via reflection, to easily switch between the mocked implementation and the final
	 * implementation.
	 * 
	 * @param methodName
	 *            eather "create" or "open"
	 * @param param
	 * 
	 * @return the created instance of VFSDiskManager
	 * @throws VFSException
	 */
	private static VFSDiskManager getDiskManager(String methodName, String[] param) throws VFSException {
		DiskConfiguration config = new DiskConfiguration();
		config.setHostFilePath(param[0]);
		if (param.length == 2)
			config.setMaximumSize(Long.parseLong(param[1]));
		try {
			Method createMethod = DISKMANAGER_IMPLEMENTATION.getMethod(methodName, DiskConfiguration.class);
			return (VFSDiskManager) createMethod.invoke(null, config);
		} catch (Exception e) {
			LOGGER.error("Error while instatiating VFSDiskManagerImplementation: ", e);
		}
		return null;
	}

	private final VFSConsole console;

	// current state of the console
	private VFSDiskManager currentManager;
	private VFSEntry currentDirectory;

	public VFSUIController(VFSConsole vfsConsole) {
		this.console = vfsConsole;

	}

	public Command getChangeDirectoryCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("cd command entered");

				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn("No disk open, please use open or create command first");
					return;
				}

				if (!(param.length == 1)) {
					LOGGER.warn("no correct number of parameters for remove command given");
					console.printHelpMessage();
					return;
				}

				if ("..".equals(param[0])) {
					currentDirectory = currentDirectory.getParent();
				}
				VFSEntry childToCD = null;
				try {
					for (VFSEntry child : currentDirectory.getChildren()) {
						if (child.getPath().getAbsolutePath().endsWith(param[0])) {
							childToCD = child;
						}
					}
					if (childToCD == null) {
						LOGGER.warn("Child: " + param[0] + " not found  in current directory");
						return;
					}
					if (!childToCD.isDirectory()) {
						LOGGER.warn("Cannot cd to file!");
						return;
					}
					currentDirectory = childToCD;

				} catch (Exception e) {
					LOGGER.error("Could not remove file:" + param[0], e);
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
					LOGGER.warn("No disk open, please use open or create command first");
					return;
				}

				try {
					currentManager.close();
				} catch (VFSException e) {
					LOGGER.error("Error while closing disk:", e);
				}
				currentManager = null;
				currentDirectory = null;
				LOGGER.debug("close command leaving");

			}
		};
	}

	public Command getCopyCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("cp command entered");
				console.stop();
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
					LOGGER.warn("not enough params for create");
					console.printHelpMessage();
					return;
				}
				try {
					currentManager = getDiskManager("create", param);
					currentDirectory = currentManager.getRoot();
				} catch (Exception e) {
					LOGGER.error("Exception while setting up Disk:", e);
				}
				LOGGER.debug("create command leaving");

			}

		};
	}

	public Command getRemoveCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("remove command entered");

				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn("No disk open, please use open or create command first");
					return;
				}

				if (!(param.length == 1)) {
					LOGGER.warn("no correct number of parameters for remove command given");
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
						LOGGER.warn("Child: " + param[0] + " not found in current directory, nothing removed");
						return;
					}

					childToRemove.delete();
				} catch (Exception e) {
					LOGGER.error("Could not remove file:" + param[0], e);
				}

				LOGGER.debug("remove command leaving");

			}
		};
	}

	public Command getDisposeCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("dispose command entered");
				if (currentManager == null) {
					LOGGER.warn("No disk open, please use open or create command");
					return;
				}
				try {
					currentManager.dispose();
				} catch (VFSException e) {
					LOGGER.error("Error disposing disk:", e);
				}
				currentManager = null;
				currentDirectory = null;
				LOGGER.debug("dispose command leaving");

			}
		};
	}

	public Command getExitCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("exit command entered");
				if (currentManager != null) {
					console.write("There is still a disk in use, please use close command first");
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
					LOGGER.warn("No disk open, please use open or create command first");
					return;
				}

				if (!(param.length == 2)) {
					LOGGER.warn("no correct number of parameters for import command given");
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
						LOGGER.warn("Child: " + param[0] + " not found in current directory, nothing exported");
						return;
					}
					InputStream is = childToExport.getInputStream();
					OutputStream os = new FileOutputStream(param[1]);

					ChannelUtil.fastStreamCopy(is, os);
				} catch (Exception e) {
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
				console.stop();
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
					LOGGER.warn("No disk open, please use open or create command first");
					return;
				}

				if (!(param.length == 2)) {
					LOGGER.warn("no correct number of parameters for import command given");
					console.printHelpMessage();
					return;
				}

				File importFile = new File(param[0]);
				if (!importFile.exists()) {
					LOGGER.warn("file to import does not exist:" + param[0]);
					return;
				}
				try {
					VFSEntry newFile = currentDirectory.getChildPath(param[1]).createFile();

					FileInputStream fis = new FileInputStream(param[0]);
					OutputStream os = newFile.getOutputStream(0);

					ChannelUtil.fastStreamCopy(fis, os);
				} catch (Exception e) {
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
					LOGGER.warn("No disk open, please use open or create command first");
					return;
				}
				try {
					for (VFSEntry child : currentDirectory.getChildren()) {
						console.write(child.getPath().getAbsolutePath());
					}
				} catch (Exception e) {
					LOGGER.error("Error while listing files", e);
				}
				LOGGER.debug("ls command leaving");

			}
		};
	}

	public Command getMoveCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("mv command entered");
				console.stop();
				LOGGER.debug("mv command leaving");

			}
		};
	}

	public Command getOpenCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("open command entered");

				if (param.length != 1) {
					console.write("not enough params for create");
					console.printHelpMessage();
					return;
				}
				try {
					currentManager = getDiskManager("open", param);
					currentDirectory = currentManager.getRoot();
				} catch (Exception e) {
					LOGGER.error("Exception while setting up Disk:", e);
				}
				LOGGER.debug("open command leaving");

			}
		};
	}

	public Command getMakeDirCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("makedir command entered");
				if (currentManager == null || currentDirectory == null) {
					LOGGER.warn("No disk open, please use open or create command");
					return;
				}
				if (param.length != 1) {
					LOGGER.warn("No correct number of arguments for makeDir");
					console.printHelpMessage();
					return;
				}
				try {
					currentDirectory.getChildPath(param[0]).createDirectory();
				} catch (Exception e) {
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
					LOGGER.warn("No disk open, please use open or create command");
					return;
				}
				if (param.length != 1) {
					LOGGER.warn("No correct number of arguments for makeDir");
					console.printHelpMessage();
					return;
				}
				try {
					currentDirectory.getChildPath(param[0]).createFile();
				} catch (Exception e) {
					LOGGER.error("Error while creating new directory:", e);
				}
				LOGGER.debug("makefile command leaving");

			}
		};
	}
}
