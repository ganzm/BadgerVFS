package ch.eth.jcd.badgers.vfs.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileDumpInputStream extends InputStream {

	private final FileOutputStream dumpOut;
	private final InputStream in;

	public FileDumpInputStream(InputStream in, String dumpFolder, String suffix) throws FileNotFoundException {
		this.dumpOut = new FileOutputStream(dumpFolder + File.separator + System.currentTimeMillis() + suffix);
		this.in = in;
	}

	@Override
	public int read() throws IOException {
		int result = in.read();

		if (result != -1) {
			dumpOut.write(result);
		}

		return result;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int numBytes = in.read(b, off, len);
		if (numBytes > 0) {
			dumpOut.write(b, off, numBytes);
		}
		return numBytes;
	}

	@Override
	public void close() throws IOException {
		in.close();
		dumpOut.close();
		super.close();
	}
}
