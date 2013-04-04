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

	private static final Logger LOGGER = Logger.getLogger(VFSConsole.class);

	private static final String CD_STRING = "cd";
	private static final String CLOSE_STRING = "close";
	private static final String CP_STRING = "cp";
	private static final String CREATE_STRING = "create";
	private static final String DISPOSE_STRING = "dispose";
	private static final String EXIT_STRING = "exit";
	private static final String FIND_STRING = "find";
	private static final String EXPORT_STRING = "export";
	private static final String IMPORT_STRING = "import";
	private static final String LS_STRING = "ls";
	private static final String MKDIR_STRING = "mkdir";
	private static final String MKFILE_STRING = "mkfile";
	private static final String MV_STRING = "mv";
	private static final String OPEN_STRING = "open";
	private static final String PWD_STRING = "pwd";
	private static final String RM_STRING = "rm";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DOMConfigurator.configure("log4j.xml");
		LOGGER.info("VFSConsole starts");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		new VFSConsole(reader, new PrintWriter(System.out)).run();
	}

	private final Map<String, Command> commands = new HashMap<String, Command>();

	private final VFSUIController controller;
	private String promptString = ">";
	private final BufferedReader reader;
	private boolean stopped = false;

	private final PrintWriter writer;

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
		commands.put(FIND_STRING, controller.getFindCommand());
		commands.put(IMPORT_STRING, controller.getImportCommand());
		commands.put(EXPORT_STRING, controller.getExportCommand());
		commands.put(CLOSE_STRING, controller.getCloseCommand());
		commands.put(PWD_STRING, controller.getPWDCommand());

	}

	public void printHelpMessage() {
		/*
		 * @formatter:off
		 */
		String helpMessage = "usage:\n"
				+ "management mode:\n"
				+ "\tcreate [c:\\path\\to\\disk.bfs 1024]\t creates virtual disk with a maximum quota of 1024 megabytes on the host system. The file may grow up to 1024 megabytes.\n"
				+ "\tdispose [c:\\path\\to\\disk.bfs]\t deletes the given virtual disk\n" 
				+ "\topen [c:\\path\\to\\disk.bfs]\t opens filesystem mode for the given virtual disk\n"
				+ "\texit\t exits the console program\n"
				+ "filesystem mode:\n"
				+ "\tls: lists current directory\n"
				+ "\tpwd: shows path to current directory\n"
				+ "\tcd [dst]\t\t changes current directory to dst which must be either a child directory of the current path or “..”\n"
				+ "\tmkdir [dirName]\t\t creates a new directory dirName in the current path\n"
				+ "\tmkfile [fileName]\t creates a new empty file fileName in the current path - this is rather not usefull, as the “import” creates a file with content\n"
				+ "\trm [file]\t\t deletes the entry denoted as file, it must be a child of the current path\n"
				+ "\tcp [src] [dst]\t\t copies the src file to dst as a child of the current path\n"
				+ "\tmv [src] [dst]\t\t moves the src file to dst\n"
				+ "\timport [ext_src] [dst]\t imports a ext src from the host system to dst\n"
				+ "\texport [src] [ext_src]\t exports a src file to the host system ext dst\n"
				+ "\tfind [searchString]\t lists all filesystem entries below the current entry containing searchString\n"
				+ "\tclose\t\t\t closes the filesystem mode, from now on management mode commands can be executed\n";
		writeLn(helpMessage);
		/*
		 * @formatter:on
		 */
	}

	/**
	 * runs the input loop until the stop flag is set by the controller
	 */
	public void run() {
		String input;

		try {
			write(promptString);

			while ((input = reader.readLine()) != null) {

				LOGGER.debug(input);
				if ("".equals(input)) {
					write(promptString);
					continue;
				}
				// splits the string at whitespaces
				// http://stackoverflow.com/questions/225337/how-do-i-split-a-string-with-any-whitespace-chars-as-delimiters
				String[] splittedInput = input.split("\\s+");
				Command cmd;
				if ((cmd = commands.get(splittedInput[0])) == null) {
					writeLn(input + " is not a valid input");
					printHelpMessage();
					write(promptString);
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
				write(promptString);

			}
		} catch (IOException e) {
			LOGGER.error(e);
		}
	}

	public void setPromptString(String promptString) {
		this.promptString = promptString;
	}

	/**
	 * called from observer to stop the input loop
	 */
	public void stop() {
		this.stopped = true;

	}

	private void write(String string) {
		writer.print(string);
		writer.flush();
	}

	public void writeLn(String string) {
		writer.println(string);
		writer.flush();
	}

}
