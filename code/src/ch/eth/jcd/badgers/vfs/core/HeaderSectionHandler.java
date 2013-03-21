package ch.eth.jcd.badgers.vfs.core;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.util.SecurityUtil;

public class HeaderSectionHandler {
	private static Logger logger = Logger.getLogger(HeaderSectionHandler.class);

	private long headerSectionOffset;

	private long headerSectionSize;

	private HeaderSectionHandler() {
	}

	public static HeaderSectionHandler createNew(RandomAccessFile randomAccessFile, DiskConfiguration config, long headerSectionOffset) throws IOException {

		logger.debug("init Header Section...");

		HeaderSectionHandler header = new HeaderSectionHandler();
		header.headerSectionOffset = headerSectionOffset;

		Charset cs = Charset.forName("ASCII");

		// go to start of the index section
		randomAccessFile.seek(header.headerSectionOffset);

		String infoString = "Badger VFS 2013 V1.0";
		randomAccessFile.write(Arrays.copyOf(infoString.getBytes(cs), 50));

		String versionString = "0.1";
		randomAccessFile.write(Arrays.copyOf(versionString.getBytes(cs), 10));

		// TODO compression
		randomAccessFile.write(Arrays.copyOf("".getBytes(cs), 20));

		// TODO encryption
		randomAccessFile.write(Arrays.copyOf("".getBytes(cs), 20));

		long indexSectionOffsetIndicatorLocation = randomAccessFile.getFilePointer();

		// write 8 bytes - we will write IndexSectionOffset later
		randomAccessFile.writeLong(0);

		// write 8 bytes - we will write DataSectionOffset later
		randomAccessFile.writeLong(0);

		// write SaltString
		byte[] saltString = new byte[8];
		new Random().nextBytes(saltString);
		randomAccessFile.write(saltString);

		// write Password Hash
		byte[] passwordHash = SecurityUtil.hashString(config.getPassword(), 50);
		randomAccessFile.write(passwordHash);

		long indexSectionOffset = randomAccessFile.getFilePointer();

		// now we are at the end of the index section, remember this position
		header.headerSectionSize = indexSectionOffset - header.headerSectionOffset;

		randomAccessFile.seek(indexSectionOffsetIndicatorLocation);

		// write IndexSectionOffset | double (8 byte)
		randomAccessFile.writeLong(indexSectionOffset);

		// DataSectionOffset | double (8 byte)
		randomAccessFile.writeLong(indexSectionOffset + IndexSectionHandler.DEFAULT_INDEXSECTION_SIZE);

		randomAccessFile.seek(indexSectionOffset);

		logger.debug("init Header Section DONE");
		return header;

	}

	public long getSectionSize() {
		return headerSectionSize;
	}

	public void close() {
		// TODO Auto-generated method stub

	}
}
