package ch.eth.jcd.badgers.vfs.encryption;

import java.io.IOException;
import java.io.OutputStream;

public class CaesarOutputStream extends OutputStream {

	private final OutputStream out;
	private final int caesarOffset;

	public CaesarOutputStream(OutputStream out, int caesarOffset) {
		this.out = out;
		this.caesarOffset = 256 - Math.abs(caesarOffset) % 256;
	}

	/**
	 * Writes the specified byte to this output stream. The general contract for write is that one byte is written to the output stream. The byte to be written
	 * is the eight low-order bits of the argument b. The 24 high-order bits of b are ignored.
	 * 
	 * 
	 * Subclasses of OutputStream must provide an implementation for this method.
	 */
	@Override
	public void write(int b) throws IOException {
		out.write(b + caesarOffset);
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void close() throws IOException {
		out.close();
	}
}
