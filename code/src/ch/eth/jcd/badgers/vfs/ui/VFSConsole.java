/**
 * JCD Virtual File System 
 * spring 2013
 * Group: Badgers
 * $Id$
 */
package ch.eth.jcd.badgers.vfs.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * TODO describe class
 * 
 */
public class VFSConsole {

	// management ommands
	private static final String CREATE_STRING = "create";
	private static final String DISPOSE_STRING = "dispose";
	private static final String OPEN_STRING = "open";
	private static final String EXIT_STRING = "exit";

	// filesystem commands
	private static final String LS_STRING = "ls";
	private static final String CD_STRING = "cd";
	private static final String MKDIR_STRING = "mkdir";
	private static final String MKFILE_STRING = "mkfile";
	private static final String RM_STRING = "rm";
	private static final String CP_STRING = "cp";
	private static final String MV_STRING = "mv";
	private static final String IMPORT_STRING = "import";
	private static final String EXPORT_STRING = "export";
	private static final String CLOSE_STRING = "close";

	private final Map<String, Command> commands = new HashMap<String, Command>();

	private static final Logger LOGGER = Logger.getLogger(VFSConsole.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DOMConfigurator.configure("log4j.xml");
		LOGGER.info("VFSConsole starts");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		new VFSConsole(reader, new PrintWriter(System.out)).run();
	}

	private final BufferedReader reader;
	private boolean stopped = false;
	private final PrintWriter writer;

	private final VFSUIController controller;

	public VFSConsole(BufferedReader reader, PrintWriter writer) {
		this.reader = reader;
		this.writer = writer;
		this.controller = new VFSUIController(this);
		initCommands();
	}

	private void initCommands() {
		commands.put(CREATE_STRING, controller.getCreateCommand());
		commands.put(DISPOSE_STRING, controller.getDisposeCommand());
		commands.put(OPEN_STRING, controller.getOpenCommand());
		commands.put(EXIT_STRING, controller.getExitCommand());

		commands.put(LS_STRING, controller.getListCommand());
		commands.put(CD_STRING, controller.getChangeDirectoryCommand());
		commands.put(MKDIR_STRING, controller.getMakeDirCommand());
		commands.put(MKFILE_STRING, controller.getMakeFileCommand());
		commands.put(RM_STRING, controller.getRemoveCommand());
		commands.put(CP_STRING, controller.getCopyCommand());
		commands.put(MV_STRING, controller.getMoveCommand());
		commands.put(IMPORT_STRING, controller.getImportCommand());
		commands.put(EXPORT_STRING, controller.getExportCommand());
		commands.put(CLOSE_STRING, controller.getCloseCommand());

	}

	/**
	 * runs the input loop until the stop flag is set by the controller
	 */
	public void run() {
		String input;

		try {
			while (((input = reader.readLine()) != null)) {

				LOGGER.debug(input);
				if ("".equals(input)) {
					continue;
				}
				// splits the string at whitespaces
				// http://stackoverflow.com/questions/225337/how-do-i-split-a-string-with-any-whitespace-chars-as-delimiters
				String[] splittedInput = input.split("\\s+");
				Command cmd;
				if ((cmd = commands.get(splittedInput[0])) == null) {
					writer.println(input + " is not a valid input");
					printHelpMessage();
					continue;
				}
				String[] params = null;

				if (splittedInput.length > 1) {

					params = Arrays.copyOfRange(splittedInput, 1, splittedInput.length);
				}
				cmd.execute(params);

				if (stopped) {
					return;
				}

			}
		} catch (IOException e) {
			LOGGER.error(e);
		}
	}

	public void printHelpMessage() {
		writer.println("Usage: TO_BE_IMPLEMENTED");

	}

	/**
	 * called from observer to stop the input loop
	 */
	public void stop() {
		this.stopped = true;

	}

	public void write(String string) {
		writer.println(string);

	}

}
