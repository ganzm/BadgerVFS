package ch.eth.jcd.badgers.vfs.compression;

import java.io.IOException;
import java.io.OutputStream;

public class BadgersRLECompressionOutputStream extends OutputStream {

	private final OutputStream out;
	private int oldRead = -1;
	private boolean duplicateFound = false;
	private int foundedDuplicates = -1;

	public BadgersRLECompressionOutputStream(OutputStream out) {
		this.out = out;
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
		if (b == oldRead) {
			duplicateFound = true;
			if (foundedDuplicates == -1) {
				out.write(b);
			}
			foundedDuplicates++;
			if (foundedDuplicates == 255) {
				out.write(foundedDuplicates);
				duplicateFound = false;
				foundedDuplicates = -1;
				oldRead = -1;
			}
			return;
		} else if (duplicateFound) {
			out.write(foundedDuplicates);
			foundedDuplicates = -1;
			duplicateFound = false;
		}
		oldRead = b;
		out.write(b);
	}

	@Override
	public void flush() throws IOException {
		if (duplicateFound && foundedDuplicates > 0) {
			out.write(foundedDuplicates);
			foundedDuplicates = -1;
			duplicateFound = false;
		}
		out.flush();
	}

	@Override
	public void close() throws IOException {
		if (duplicateFound && foundedDuplicates > 0) {
			out.write(foundedDuplicates);
			foundedDuplicates = -1;
			duplicateFound = false;
		}
		out.close();
	}
}
