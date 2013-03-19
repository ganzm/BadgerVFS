/**
 * JCD Virtual File System 
 * spring 2013
 * Group: Badgers
 */
package ch.eth.jcd.badgers.vfs.ui;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * TODO describe class
 * 
 */
public class VFSConsole {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		DOMConfigurator.configure("log4j.xml");
		Logger logger = Logger.getLogger(VFSConsole.class);
		logger.info("Log4J works");
	}
}
