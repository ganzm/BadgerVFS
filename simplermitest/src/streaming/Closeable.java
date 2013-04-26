package streaming;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Closeable extends Remote {

	void close() throws IOException, RemoteException;
}
