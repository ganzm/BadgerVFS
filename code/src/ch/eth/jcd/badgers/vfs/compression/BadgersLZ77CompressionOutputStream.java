package ch.eth.jcd.badgers.vfs.compression;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class BadgersLZ77CompressionOutputStream extends OutputStream {

	private final OutputStream out;
	private final StringBuilder cachedString = new StringBuilder();
	private final StringBuilder forwardString = new StringBuilder();
	ArrayList<BadgersLZ77Tuple> encodedInput = new ArrayList<BadgersLZ77Tuple>();

	public BadgersLZ77CompressionOutputStream(OutputStream out) {
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
		forwardString.append((char) b);
		if (forwardString.length() <= BadgersLZ77Tuple.lookForwardWindow) {
			return;
		}
		work();
	}

	@Override
	public void flush() throws IOException {
		while (forwardString.length() > 0) {
			work();
		}
		super.flush();
	}

	@Override
	public void close() throws IOException {
		while (forwardString.length() > 0) {
			work();
		}
		super.close();
	}

	private void work() throws IOException {
		String currentChar = forwardString.substring(0, 1);
		int currentMatchLocation = 0;
		int matchLength = 1;

		if (cachedString.indexOf(currentChar) != -1 && forwardString.length() > 1) {
			// find bigger match
			matchLength++;
			while (matchLength < forwardString.length()) {
				if (cachedString.indexOf(forwardString.substring(0, matchLength)) != -1 && true) {
					matchLength++;
				} else {
					break;
				}
			}
			// biggest match
			matchLength--;
			// get match location
			currentMatchLocation = cachedString.indexOf(forwardString.substring(0, matchLength));
			// remove matches from forwardString
			cachedString.append(forwardString.substring(0, matchLength));
			forwardString.delete(0, matchLength);

			// offset to the match
			int offset = cachedString.length() - (currentMatchLocation + matchLength);
			encodedInput.add(new BadgersLZ77Tuple(offset, matchLength, forwardString.substring(0, 1)));
			// System.out.println(encodedInput.get(encodedInput.size() - 1));
			byte[] tmp = encodedInput.get(encodedInput.size() - 1).toByte();
			out.write(tmp[0]);
			out.write(tmp[1]);
			out.write(tmp[2]);

		} else {
			// match not founde -> create Tuple
			encodedInput.add(new BadgersLZ77Tuple(0, 0, currentChar));
			// System.out.println(encodedInput.get(encodedInput.size() - 1));
			byte[] tmp = encodedInput.get(encodedInput.size() - 1).toByte();
			out.write(tmp[0]);
			out.write(tmp[1]);
			out.write(tmp[2]);
		}
		cachedString.append(forwardString.substring(0, 1));
		forwardString.deleteCharAt(0);
		while (cachedString.length() > BadgersLZ77Tuple.windowLength) {
			cachedString.deleteCharAt(0);
		}
	}
}
