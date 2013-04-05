package ch.eth.jcd.badgers.vfs.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileDumpOutputStream extends OutputStream {

	private final FileOutputStream dumpOut;
	private final OutputStream out;

	public FileDumpOutputStream(OutputStream out, String dumpFolder, String suffix) throws FileNotFoundException {
		this.dumpOut = new FileOutputStream(dumpFolder + File.separator + System.currentTimeMillis() + suffix);
		this.out = out;
	}

	@Override
	public void write(int b) throws IOException {
		dumpOut.write(b);
		out.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		dumpOut.write(b, off, len);
		out.write(b, off, len);
	}

	@Override
	public void close() throws IOException {
		out.close();
		dumpOut.close();
		super.close();
	}
}
