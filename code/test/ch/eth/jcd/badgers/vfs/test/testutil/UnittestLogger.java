package ch.eth.jcd.badgers.vfs.test.testutil;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.SimpleLayout;

public class UnittestLogger {

	/**
	 * Init Log4j for UnitTests
	 */
	public static void init() {
		BasicConfigurator.configure(new ConsoleAppender(new SimpleLayout()));
	}
}
