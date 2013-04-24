package ch.eth.jcd.badgers.vfs.ui.desktop;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import ch.eth.jcd.badgers.vfs.sync.server.ServerConfiguration;

public class Initialisation {

	private static final Logger LOGGER = Logger.getLogger(Initialisation.class);

	public static void initLog4J(String[] args) {
		String log4JConfigurationPathFromConsole = null;

		for (int i = 0; i < args.length; i++) {
			if ("-l".equals(args[i]) && i + 1 < args.length) {
				log4JConfigurationPathFromConsole = args[i + 1];
			}
		}

		String log4JConfigurationPath = log4JConfigurationPathFromConsole == null ? "log4j.xml" : log4JConfigurationPathFromConsole;

		DOMConfigurator.configure(log4JConfigurationPath);
		LOGGER.info("Log4J initialized with " + log4JConfigurationPath);
	}

	public static ServerConfiguration parseServerConfiguration(String[] args) {
		ServerConfiguration config = new ServerConfiguration();

		return config;
	}

}
