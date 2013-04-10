package ch.eth.jcd.badgers.vfs.compression;

import java.io.IOException;
import java.io.OutputStream;

public class BadgersLZ77CompressionOutputStream extends OutputStream {

	private final OutputStream out;
	private final StringBuilder cachedString = new StringBuilder();
	private final StringBuilder forwardString = new StringBuilder();

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
		if (forwardString.length() <= BadgersLZ77Tuple.LOOK_FORWARD_WINDOW) {
			return;
		}
		work();
	}

	@Override
	public void flush() throws IOException {
		while (forwardString.length() > 0) {
			work();
		}
		out.flush();
	}

	@Override
	public void close() throws IOException {
		while (forwardString.length() > 0) {
			work();
		}
		out.close();
	}

	private void work() throws IOException {
		String currentChar = forwardString.substring(0, 1);
		int currentMatchLocation = 0;
		int matchLength = 1;

		int currentMatchLocationTmp = cachedString.indexOf(currentChar);
		if (currentMatchLocationTmp != -1 && forwardString.length() > 1) {
			currentMatchLocation = currentMatchLocationTmp;
			// find bigger match
			matchLength++;
			while (matchLength < forwardString.length()) {
				currentMatchLocationTmp = cachedString.indexOf(forwardString.substring(0, matchLength), currentMatchLocation);
				if (currentMatchLocationTmp == -1) {
					break;
				} else {
					currentMatchLocation = currentMatchLocationTmp;
					matchLength++;
				}
			}
			// biggest match
			matchLength--;
			// remove matches from forwardString
			cachedString.append(forwardString.substring(0, matchLength));
			forwardString.delete(0, matchLength);

			// offset to the match
			int offset = cachedString.length() - currentMatchLocation - matchLength;
			write(out, offset, matchLength, forwardString.substring(0, 1));

		} else {
			// match not found -> create Tuple
			write(out, 0, 0, currentChar);
		}
		cachedString.append(forwardString.substring(0, 1));
		forwardString.deleteCharAt(0);
		while (cachedString.length() > BadgersLZ77Tuple.WINDOW_LENGTH) {
			cachedString.deleteCharAt(0);
		}
	}

	private void write(OutputStream out, int matchLoc, int matchLength, String charFollowed) throws IOException {
		byte[] writeByteBuffer = new byte[3];

		int concat = (matchLoc << 4) | matchLength;
		writeByteBuffer[0] = (byte) (concat >> 8);
		writeByteBuffer[1] = (byte) concat;
		writeByteBuffer[2] = (byte) (charFollowed.charAt(0));

		out.write(writeByteBuffer, 0, 3);
	}
}
