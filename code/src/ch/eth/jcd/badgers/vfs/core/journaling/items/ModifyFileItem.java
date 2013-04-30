package ch.eth.jcd.badgers.vfs.core.journaling.items;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.VFSFileImpl;
import ch.eth.jcd.badgers.vfs.core.data.DataBlock;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.remote.streaming.RemoteInputStreamServer;

public class ModifyFileItem extends JournalItem {

	private static final long serialVersionUID = -1463585225400507478L;

	protected static final Logger LOGGER = Logger.getLogger(ModifyFileItem.class);

	private final String absolutePath;

	private InputStream inputStream;

	public ModifyFileItem(VFSFileImpl vfsFileImpl) throws VFSException {
		this.absolutePath = vfsFileImpl.getPath().getAbsolutePath();
		this.inputStream = vfsFileImpl.getInputStream();
	}

	@Override
	public void beforeRmiTransport() throws VFSException {
		try {
			this.inputStream = RemoteInputStreamServer.wrap(inputStream);
		} catch (RemoteException ex) {
			throw new VFSException(ex);
		}
	}

	@Override
	public void beforeSerializeToDisk() throws VFSException {
		inputStream = null;
	}

	@Override
	public void afterDeserializeFromDisk(VFSDiskManager diskManager) throws VFSException {
		VFSPath path = diskManager.createPath(absolutePath);
		this.inputStream = path.getVFSEntry().getInputStream();
	}

	@Override
	public void doReplay(VFSDiskManager diskManager) throws VFSException {
		LOGGER.debug("Journal - Modify File " + absolutePath);
		VFSPath filePath = diskManager.createPath(absolutePath);
		VFSEntry file = filePath.getVFSEntry();

		// copy file
		try (OutputStream out = file.getOutputStream(VFSEntry.WRITE_MODE_OVERRIDE)) {
			byte[] buffer = new byte[DataBlock.USERDATA_SIZE];
			int numBytes;
			while ((numBytes = inputStream.read(buffer)) > 0) {
				out.write(buffer, 0, numBytes);
			}
		} catch (IOException e) {
			throw new VFSException(e);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				throw new VFSException(e);
			}
			inputStream = null;
		}
	}
}
