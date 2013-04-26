package ch.eth.jcd.badgers.vfs.remote.streaming;

import java.io.IOException;
import java.rmi.RemoteException;

public interface Writeable extends Closeable {

	/**
	 * Writes given byte block to the stream.
	 * 
	 * @param data
	 * @throws IOException
	 * @throws RemoteException
	 */
	void write(byte data[]) throws IOException, RemoteException;
}
