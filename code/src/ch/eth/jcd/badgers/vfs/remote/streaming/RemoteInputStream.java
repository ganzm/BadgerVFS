package ch.eth.jcd.badgers.vfs.remote.streaming;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class RemoteInputStream extends InputStream implements Serializable {

	private static final int BUFFER_SIZE = 1024;
	private final static int MAX_EXP = 12; // max fetch size = 2^MAX_EXP (4 MB)
	private static final long serialVersionUID = 1L;

	private final Readable source;
	private byte buffer[];
	private int pos;
	private int exp;
	private boolean closed;

	RemoteInputStream(final Readable source) {
		this.source = source;
	}

	@Override
	public int read() throws IOException {

		if (pos == -2) {
			return -1;
		}
		if (buffer == null || pos > buffer.length - 1) {
			buffer = source.read(BUFFER_SIZE * (exp > MAX_EXP ? 1 << MAX_EXP : 1 << exp++));
			pos = 0;
			if (buffer.length == 0) {
				pos = -2;
				return -1;

			}
		}
		return buffer[pos++] & 0xff;
	}

	@Override
	public void close() throws IOException {
		// we have to check this, because otherwise we get RMI exceptions,
		// that the "source" object cannot be found in registry
		if (!closed) {
			source.close();
			closed = true;
		}
	}
}
