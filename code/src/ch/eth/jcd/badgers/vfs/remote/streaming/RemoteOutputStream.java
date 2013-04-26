package ch.eth.jcd.badgers.vfs.remote.streaming;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

public class RemoteOutputStream extends OutputStream implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Writeable target;

	private byte buffer[] = new byte[1024];
	private int pos = -1;
	private int exp;
	private final static int MAX_EXP = 6; // 2^MAX_EXP (64 KB) = buffer size

	RemoteOutputStream(final Writeable target) {
		this.target = target;
	}

	@Override
	public void write(final int b) throws IOException {

		buffer[++pos] = (byte) b;
		if (pos >= buffer.length - 1) {
			target.write(buffer);
			pos = -1;
			if (exp <= MAX_EXP) {
				buffer = new byte[1024 * (1 << exp++)];
			}
		}
	}

	@Override
	public void flush() throws IOException {

		if (pos > -1) {
			final byte pendingData[] = new byte[pos + 1];
			System.arraycopy(buffer, 0, pendingData, 0, pendingData.length);
			target.write(pendingData);
			pos = -1;
		}
	}

	@Override
	public void close() throws IOException {
		flush();
		target.close();
	}
}
