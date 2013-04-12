package ch.eth.jcd.badgers.vfs.util;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

public class SwingUtil {

	private static final Logger LOGGER = Logger.getLogger(SwingUtil.class);

	public static void handleException(Component parent, Exception ex) {
		LOGGER.error("", ex);
		JOptionPane.showMessageDialog(parent, ex.getClass().getName() + ":" + ex.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
	}

	public static void handleError(Component parent, String error) {
		LOGGER.error(error);
		JOptionPane.showMessageDialog(parent, error, "Exception", JOptionPane.ERROR_MESSAGE);
	}
}
