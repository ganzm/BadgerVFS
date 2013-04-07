package ch.eth.jcd.badgers.vfs.encryption;

import java.io.IOException;
import java.io.InputStream;

public class CaesarInputStream extends InputStream {

	private final InputStream in;
	private final int caesarOffset;

	public CaesarInputStream(InputStream in, int caesarOffset) {
		this.in = in;
		this.caesarOffset = Math.abs(caesarOffset) % 256;
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
		int data = in.read();
		if (data >= 0) {
			return (data + caesarOffset) % 256;

		} else {
			return data;
		}
	}

	@Override
	public void close() throws IOException {
		in.close();
	}
}
