package ch.eth.jcd.badgers.vfs.ui.desktop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.sync.server.ServerConfiguration;

public class Initialisation {

	private static final Logger LOGGER = Logger.getLogger(Initialisation.class);

	public static final String HELP_MESSAGE_SYNC_SERVER = "usage:\n" + "-cc\t\tClear configuration: deletes all server side data\n"
			+ "-c [path]\tfolder where the synchronisation server puts it's data\n" + "-l [log4j.xml]\tpath to the Log4J configuration file to use\n";

	public static void initLog4J(final String[] args) {
		String log4JConfigurationPathFromConsole = null;

		for (int i = 0; i < args.length; i++) {
			if ("-l".equals(args[i]) && i + 1 < args.length) {
				log4JConfigurationPathFromConsole = args[i + 1];
			}
		}

		final String log4JConfigurationPath = log4JConfigurationPathFromConsole == null ? "log4j.xml" : log4JConfigurationPathFromConsole;

		DOMConfigurator.configure(log4JConfigurationPath);
		LOGGER.info("Log4J initialized with " + log4JConfigurationPath);
	}

	public static String getCommandLineArgumentValue(final String[] args, String argName) {
		for (int i = 0; i < args.length; i++) {
			if (argName.equals(args[i]) && i + 1 < args.length) {
				return args[i + 1];
			}
		}

		return null;
	}

	public static boolean hasCommandLineArgument(final String[] args, String argName) {
		for (String arg : args) {
			if (argName.equals(arg)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasCommandLineArgument(final String[] args, List<String> argNames) {
		for (String arg : args) {
			if (argNames.contains(arg)) {
				return true;
			}
		}
		return false;
	}

	public static ServerConfiguration parseServerConfiguration(final String[] args) throws VFSException {
		List<String> helpArgs = new ArrayList<>();
		helpArgs.add("-?");
		helpArgs.add("-help");
		helpArgs.add("-h");
		if (hasCommandLineArgument(args, helpArgs)) {
			System.out.println(HELP_MESSAGE_SYNC_SERVER);
		}

		// -c config folder path flag
		String configPath = getCommandLineArgumentValue(args, "-c");

		if (configPath == null || configPath.isEmpty()) {
			configPath = ServerConfiguration.DEFAULT_SERVER_FOLDER;
		}

		// clear config flag
		boolean clearConfiguration = hasCommandLineArgument(args, "-cc");
		if (clearConfiguration) {
			LOGGER.info("Reset SynchronisationServer Configuration at " + configPath);
			File configFolder = new File(configPath);
			if (configFolder.exists()) {
				if (!configFolder.delete()) {
					LOGGER.warn("Could not delete Config at " + configPath);
				}
			}
		}

		final ServerConfiguration config = new ServerConfiguration(configPath);

		return config;
	}
}
