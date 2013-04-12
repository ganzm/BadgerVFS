package ch.eth.jcd.badgers.vfs.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

public class ResourceLocator {
	private static final Logger LOGGER = Logger.getLogger(ResourceLocator.class);

	public static InputStream getResource(String name) {
		LOGGER.debug("getResource " + name);
		return ResourceLocator.class.getClassLoader().getResourceAsStream(name);
	}

	public static ImageIcon getResourceAsIcon(String name) throws IOException {
		LOGGER.debug("getResourceAsIcon " + name);

		try (InputStream input = getResource(name);) {
			ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
			byte[] buffer = new byte[512];
			int numBytes;
			while ((numBytes = input.read(buffer)) >= 0) {
				byteArr.write(buffer, 0, numBytes);
			}

			return new ImageIcon(byteArr.toByteArray());
		}
	}
}
