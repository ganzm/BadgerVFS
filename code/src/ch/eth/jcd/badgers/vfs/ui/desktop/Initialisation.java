package ch.eth.jcd.badgers.vfs.ui.desktop;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class Initialisation {

	private static final Logger LOGGER = Logger.getLogger(Initialisation.class);

	public static void initApplication(String[] args) {
		String log4JConfigurationPath = null;
		for (int i = 0; i < args.length; i++) {

			if ("-l".equals(args[i]) && (i + 1 < args.length)) {

				log4JConfigurationPath = args[i + 1];

			}

		}

		initLog4J(log4JConfigurationPath);
	}

	private static void initLog4J(String log4jConfigurationPath) {
		if (log4jConfigurationPath == null) {
			log4jConfigurationPath = "log4j.xml";

		}
		DOMConfigurator.configure(log4jConfigurationPath);
		LOGGER.info("Log4J initialized with " + log4jConfigurationPath);
	}
}
