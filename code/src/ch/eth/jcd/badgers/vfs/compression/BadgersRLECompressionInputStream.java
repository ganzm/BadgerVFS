package ch.eth.jcd.badgers.vfs.compression;

import java.io.IOException;
import java.io.InputStream;

public class BadgersRLECompressionInputStream extends InputStream {

	private final InputStream in;
	private int oldRead = -1;
	private int bytesToReturn = -1;

	public BadgersRLECompressionInputStream(InputStream in) {
		this.in = in;
	}

	/**
	 * Reads the next byte of data from the input stream. The value byte is returned as an int in the range 0 to 255. If no byte is available because the end of
	 * the stream has been reached, the value -1 is returned. This method blocks until input data is available, the end of the stream is detected, or an
	 * exception is thrown.
	 * 
	 * A subclass must provide an implementation of this method.
	 * 
	 */
	@Override
	public int read() throws IOException {
		int tmpRead = oldRead;
		if (bytesToReturn > 0) {
			bytesToReturn--;
			if (bytesToReturn == 0) {
				oldRead = -1;
			}
			return tmpRead;
		}
		int newRead = in.read();
		if (newRead == oldRead) {
			bytesToReturn = in.read();
			if (bytesToReturn == 0) {
				oldRead = -1;
			}
			return tmpRead;

		}
		oldRead = newRead;
		return newRead;

	}
}
