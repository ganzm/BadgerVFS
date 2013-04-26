package ch.eth.jcd.badgers.vfs.remote.streaming;

import java.io.IOException;
import java.rmi.RemoteException;

public interface Readable extends Closeable {

	/**
	 * Reads at most count bytes from a stream.
	 * 
	 * @param count
	 * @return data read as a byte array
	 * @throws IOException
	 * @throws RemoteException
	 */
	byte[] read(int count) throws IOException, RemoteException;
}
