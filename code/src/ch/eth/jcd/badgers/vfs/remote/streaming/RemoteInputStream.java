package ch.eth.jcd.badgers.vfs.remote.streaming;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Classes in this package are taken from here
 * 
 * <a href="http://java.dzone.com/articles/java-io-streams-and-rmi"/> and fixed/adapted to VFS Synchronization Server purposes.
 */
public class RemoteInputStream extends InputStream implements Serializable {
	private static final long serialVersionUID = 4102456704778084627L;

	private final Readable source;
	private boolean closed;

	RemoteInputStream(final Readable source) {
		this.source = source;
	}

	@Override
	public int read() throws IOException {
		byte[] buffer = new byte[1];

		buffer = source.read(1);
		if (buffer.length == 0) {
			return -1;
		}

		return buffer[0] & 0xff;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		byte[] result = source.read(len);
		for (int i = 0; i < result.length; i++) {
			b[off + i] = result[i];
		}

		return result.length;
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
