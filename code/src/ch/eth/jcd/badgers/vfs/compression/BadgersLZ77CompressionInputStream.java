package ch.eth.jcd.badgers.vfs.compression;

import java.io.IOException;
import java.io.InputStream;

public class BadgersLZ77CompressionInputStream extends InputStream {

	private final InputStream in;
	private final StringBuilder cachedString = new StringBuilder();
	private BadgersLZ77Tuple currentTuple;

	public BadgersLZ77CompressionInputStream(InputStream in) {
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
		while (cachedString.length() > BadgersLZ77Tuple.WINDOW_LENGTH) {
			cachedString.deleteCharAt(0);
		}
		if (currentTuple != null && currentTuple.matchLength == 0) {
			cachedString.append(currentTuple.charFollowed);
			currentTuple.matchLength--;
			return currentTuple.charFollowed.charAt(0);
		}
		if (currentTuple != null && currentTuple.matchLength > 0) {
			char charToReturn = cachedString.charAt(cachedString.length() - currentTuple.matchLoc);
			cachedString.append(charToReturn);
			currentTuple.matchLength--;
			return charToReturn;
		}

		int newRead = in.read();
		if (newRead == -1) {
			return newRead;
		}
		currentTuple = new BadgersLZ77Tuple(newRead, in.read(), in.read());

		if (currentTuple.matchLength == 0) {
			// no match, just return;
			cachedString.append(currentTuple.charFollowed);
			currentTuple.matchLength--;
			return currentTuple.charFollowed.charAt(0);
		} else {
			// match found, return first char and append it to the cachedString.
			char charToReturn = cachedString.charAt(cachedString.length() - currentTuple.matchLoc);
			cachedString.append(charToReturn);
			currentTuple.matchLength--;
			return charToReturn;
		}
	}
}
