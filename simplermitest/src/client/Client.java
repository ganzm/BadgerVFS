package client;

import interfaces.LoginService;
import interfaces.VFSService;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import streaming.ChannelUtil;
import streaming.RemoteInputStream;
import streaming.RemoteInputStreamServer;
import streaming.RemoteOutputStream;
import streaming.RemoteOutputStreamServer;

public class Client {
	public static void main(final String[] args) {

		final String host = (args.length < 1) ? null : args[0];
		try {
			final Registry registry = LocateRegistry.getRegistry(host);
			final LoginService ls = (LoginService) registry.lookup(LoginService.LOGIN_SERVICE_KEY);
			final VFSService vfsService = ls.doLogin("foouser", "barpw");

			System.out.println("createdFile: " + vfsService.createFile("fooofile"));
			final FileInputStream fis = new FileInputStream("/home/rop/Desktop/import.log");
			RemoteOutputStream ros = vfsService.getOutputStream("output.log");
			ChannelUtil.fastStreamCopy(fis, ros);

			final FileOutputStream fos = new FileOutputStream("/home/rop/Desktop/exported.log");
			RemoteInputStream ris = vfsService.getInputStream("output.log");

			ChannelUtil.fastStreamCopy(ris, fos);

			ris = RemoteInputStreamServer.wrap(new FileInputStream("/home/rop/Desktop/import.log"));
			vfsService.uploadFile(ris, "uploaded.log");

			ros = RemoteOutputStreamServer.wrap(new FileOutputStream("/home/rop/Desktop/downloaded.log"));
			vfsService.downloadFile(ros, "output.log");
			System.out.println("logout: " + vfsService.disconnect());

		} catch (final Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
	}
}
