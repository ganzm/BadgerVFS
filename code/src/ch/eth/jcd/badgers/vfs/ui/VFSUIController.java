/**
 * JCD Virtual File System 
 * spring 2013
 * Group: Badgers
 * $Id$
 */
package ch.eth.jcd.badgers.vfs.ui;

import org.apache.log4j.Logger;

/**
 * $Id$ TODO describe VFSUIController
 * 
 */
public class VFSUIController {
	private static final Logger LOGGER = Logger.getLogger(VFSUIController.class);

	private final VFSConsole vfsConsole;

	public VFSUIController(VFSConsole vfsConsole) {
		this.vfsConsole = vfsConsole;
	}

	public Command getChangeDirectoryCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				// TODO Auto-generated method stub

			}
		};
	}

	public Command getCloseCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("Close command entered");
				vfsConsole.stop();
				LOGGER.debug("Close command leaving");

			}
		};
	}

	public Command getCopyCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				// TODO Auto-generated method stub

			}
		};
	}

	public Command getCreateCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				// TODO Auto-generated method stub

			}
		};
	}

	public Command getDeleteCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				// TODO Auto-generated method stub

			}
		};
	}

	public Command getDisposeCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				// TODO Auto-generated method stub

			}
		};
	}

	public Command getExitCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				LOGGER.debug("exit command entered");
				vfsConsole.stop();
				LOGGER.debug("exit command leaving");
			}
		};
	}

	public Command getExportCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				// TODO Auto-generated method stub

			}
		};
	}

	public Command getFindCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				// TODO Auto-generated method stub

			}
		};
	}

	public Command getImportCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				// TODO Auto-generated method stub

			}
		};
	}

	public Command getListCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				// TODO Auto-generated method stub

			}
		};
	}

	public Command getMoveCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				// TODO Auto-generated method stub

			}
		};
	}

	public Command getOpenCommand() {
		return new Command() {

			@Override
			public void execute(String[] param) {
				// TODO Auto-generated method stub

			}
		};
	}
}
