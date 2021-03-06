package ch.eth.jcd.badgers.vfs.remote.streaming;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteInputStreamServer implements Readable {

	private final InputStream in;
	private boolean alreadyUnexported = false;

	public RemoteInputStreamServer(final InputStream in) {
		this.in = in;
	}

	@Override
	public byte[] read(final int count) throws IOException, RemoteException {

		final byte buffer[] = new byte[count];
		final int actualCount = in.read(buffer);
		if (actualCount == count) {
			return buffer;
		} else if (actualCount == -1) {
			return new byte[0];
		} else {
			final byte data[] = new byte[actualCount];
			System.arraycopy(buffer, 0, data, 0, data.length);
			return data;
		}
	}

	@Override
	public void close() throws IOException, RemoteException {

		try {
			in.close();
		} finally {
			// we have to check this, because otherwise we get RMI exceptions,
			// that the "source" object cannot be found in registry
			if (!alreadyUnexported) {
				UnicastRemoteObject.unexportObject(this, true);
				alreadyUnexported = true;
			}
		}
	}

	public static RemoteInputStream wrap(final InputStream in) throws RemoteException {

		return new RemoteInputStream((Readable) UnicastRemoteObject.exportObject(new RemoteInputStreamServer(in), 0));
	}
}
