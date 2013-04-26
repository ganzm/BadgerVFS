package server;

import interfaces.VFSService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.rmi.RemoteException;

import streaming.ChannelUtil;
import streaming.RemoteInputStream;
import streaming.RemoteInputStreamServer;
import streaming.RemoteOutputStream;
import streaming.RemoteOutputStreamServer;

public class VFSServiceImpl implements VFSService {

	private final LoginServiceImpl loginSerivce;
	private final String uuid;
	private static final String TEMP_FOLDER = System.getProperty("java.io.tmpdir");

	public VFSServiceImpl(final LoginServiceImpl loginService, final String uuid) {
		this.loginSerivce = loginService;
		this.uuid = uuid;
	}

	@Override
	public boolean createFile(final String fileName) {
		return true;
	}

	@Override
	public boolean disconnect() throws RemoteException {
		return loginSerivce.disconnect(this);
	}

	public String getId() {
		// TODO Auto-generated method stub
		return uuid;
	}

	@Override
	public RemoteOutputStream getOutputStream(final String fileName) {
		try {
			final File outputFile = new File(TEMP_FOLDER, fileName);
			System.out.println("getOutputStream: " + outputFile.getAbsolutePath());
			final FileOutputStream fio = new FileOutputStream(outputFile);
			return RemoteOutputStreamServer.wrap(fio);
		} catch (final FileNotFoundException | RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public RemoteInputStream getInputStream(final String fileName) throws RemoteException {
		FileInputStream fis;
		try {
			final File inputFile = new File(TEMP_FOLDER, fileName);
			System.out.println("getInputStream from " + inputFile.getAbsolutePath());
			fis = new FileInputStream(inputFile);
			return RemoteInputStreamServer.wrap(fis);
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean uploadFile(final RemoteInputStream inputStream, final String fileName) throws RemoteException {
		try {

			final File tempFile = new File(TEMP_FOLDER, fileName);
			System.out.println("UploadFile to " + tempFile.getAbsolutePath());
			final FileOutputStream fos = new FileOutputStream(tempFile);
			ChannelUtil.fastStreamCopy(inputStream, fos);
			return true;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean downloadFile(final RemoteOutputStream outputStream, final String fileName) throws RemoteException {
		try {

			final File inputFile = new File(TEMP_FOLDER, fileName);
			System.out.println("downloadFile from " + inputFile.getAbsolutePath());
			ChannelUtil.fastStreamCopy(new FileInputStream(inputFile), outputStream);
			return true;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
