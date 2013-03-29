package ch.eth.jcd.badgers.vfs.test.testutil;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;

public class UnittestLogger {

	private static boolean loggerInitialized = false;

	/**
	 * Init Log4j for UnitTests
	 */
	public static void init() {
		if (!loggerInitialized) {
			BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%-5p [%t] %-80c %m%n")));
		}
		loggerInitialized = true;
	}
}
