package ch.eth.jcd.badgers.vfs.remote.streaming;

import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RemoteOutputStreamServer implements Writeable {

	private final OutputStream out;

	public RemoteOutputStreamServer(final OutputStream out) {
		this.out = out;
	}

	@Override
	public void write(final byte data[]) throws IOException, RemoteException {
		out.write(data);
	}

	@Override
	public void close() throws IOException, RemoteException {

		try {
			out.flush();
			out.close();
		} finally {
			UnicastRemoteObject.unexportObject(this, true);
		}
	}

	public static RemoteOutputStream wrap(final OutputStream out) throws RemoteException {

		return new RemoteOutputStream((Writeable) UnicastRemoteObject.exportObject(new RemoteOutputStreamServer(out), Registry.REGISTRY_PORT));
	}

}
