package ch.eth.jcd.badgers.vfs.core.header;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.directory.DirectorySectionHandler;
import ch.eth.jcd.badgers.vfs.util.ByteUtil;
import ch.eth.jcd.badgers.vfs.util.SecurityUtil;

public class HeaderSectionHandler {
	private static Logger logger = Logger.getLogger(HeaderSectionHandler.class);

	private static Charset cs = Charset.forName("ASCII");

	private static int INFO_FIELD_LENGTH = 50;
	private static int VERSION_FIELD_LENGTH = 10;
	private static int COMPRESSION_FIELD_LENGTH = 20;
	private static int ENCRYPTION_FIELD_LENGTH = 20;
	private static int SALT_FIELD_LENGTH = 20;
	private static int PASSWORDHASH_FIELD_LENGTH = 20;

	private static String defaultInfoString = "Badger VFS 2013 V1.0";
	private static String defaultVersionString = "0.1";

	private String infoString;

	private String versionString;

	private long headerSectionOffset;

	private long headerSectionSize;

	private String compressionString;

	private String encryptionString;

	private byte[] salt;

	private byte[] passwordHash;

	private long directorySectionOffset;

	private long dataSectionOffset;

	private final RandomAccessFile virtualDiskFile;

	private HeaderSectionHandler(RandomAccessFile virtualDiskFile) {
		this.virtualDiskFile = virtualDiskFile;
	}

	public static HeaderSectionHandler createNew(RandomAccessFile virtualDiskFile, DiskConfiguration config, long headerSectionOffset) throws IOException {

		logger.debug("init Header Section...");

		HeaderSectionHandler header = new HeaderSectionHandler(virtualDiskFile);
		header.headerSectionOffset = headerSectionOffset;

		// go to start of the index section
		virtualDiskFile.seek(header.headerSectionOffset);

		header.infoString = defaultInfoString;
		virtualDiskFile.write(Arrays.copyOf(header.infoString.getBytes(cs), INFO_FIELD_LENGTH));

		header.versionString = defaultVersionString;
		virtualDiskFile.write(Arrays.copyOf(header.versionString.getBytes(cs), VERSION_FIELD_LENGTH));

		// TODO compression
		virtualDiskFile.write(Arrays.copyOf("".getBytes(cs), COMPRESSION_FIELD_LENGTH));

		// TODO encryption
		virtualDiskFile.write(Arrays.copyOf("".getBytes(cs), ENCRYPTION_FIELD_LENGTH));

		long indexSectionOffsetIndicatorLocation = virtualDiskFile.getFilePointer();

		// write 8 bytes - we will write DirectorySectionOffset later
		virtualDiskFile.writeLong(0);

		// write 8 bytes - we will write DataSectionOffset later
		virtualDiskFile.writeLong(0);

		// write SaltString
		header.salt = new byte[SALT_FIELD_LENGTH];
		new Random().nextBytes(header.salt);
		virtualDiskFile.write(header.salt);

		// write Password Hash
		header.passwordHash = SecurityUtil.hashString(config.getPassword(), PASSWORDHASH_FIELD_LENGTH);
		virtualDiskFile.write(header.passwordHash);

		header.directorySectionOffset = virtualDiskFile.getFilePointer();

		// now we are at the end of the directory section, remember this position
		header.headerSectionSize = header.directorySectionOffset - header.headerSectionOffset;

		virtualDiskFile.seek(indexSectionOffsetIndicatorLocation);

		// write IndexSectionOffset | double (8 byte)
		virtualDiskFile.writeLong(header.directorySectionOffset);

		// DataSectionOffset | double (8 byte)
		header.dataSectionOffset = header.directorySectionOffset + DirectorySectionHandler.DEFAULT_DIRECTORYSECTION_SIZE;
		virtualDiskFile.writeLong(header.dataSectionOffset);

		virtualDiskFile.seek(header.directorySectionOffset);

		logger.debug("init Header Section DONE");
		return header;

	}

	public static HeaderSectionHandler createExisting(RandomAccessFile virtualDiskFile, DiskConfiguration config, long headerSectionOffset) throws IOException {
		logger.debug("reading Header Section...");

		HeaderSectionHandler header = new HeaderSectionHandler(virtualDiskFile);
		header.headerSectionOffset = headerSectionOffset;

		Charset cs = Charset.forName("ASCII");

		// go to start of the index section
		virtualDiskFile.seek(header.headerSectionOffset);

		// Read Info String
		byte[] infoBytes = new byte[INFO_FIELD_LENGTH];
		virtualDiskFile.read(infoBytes);
		header.infoString = new String(infoBytes, cs).trim();
		logger.debug("Info: " + header.infoString);

		// Read Version String
		byte[] versionBytes = new byte[VERSION_FIELD_LENGTH];
		virtualDiskFile.read(versionBytes);
		header.versionString = new String(versionBytes, cs).trim();
		logger.debug("Version: " + header.versionString);

		// TODO Read compression
		byte[] compressionBytes = new byte[COMPRESSION_FIELD_LENGTH];
		virtualDiskFile.read(compressionBytes);
		header.compressionString = new String(compressionBytes, cs);
		logger.debug("Compression: " + header.compressionString);

		// TODO Read compression
		byte[] encryptionBytes = new byte[ENCRYPTION_FIELD_LENGTH];
		virtualDiskFile.read(encryptionBytes);
		header.encryptionString = new String(encryptionBytes, cs);
		logger.debug("Encryption: " + header.encryptionString);

		// read 8 bytes - DirectorySectionOffset
		header.directorySectionOffset = virtualDiskFile.readLong();
		logger.debug("DirectorySectionOffset: " + header.directorySectionOffset);

		// read 8 bytes - DataSectionOffset
		header.dataSectionOffset = virtualDiskFile.readLong();
		header.headerSectionSize = header.dataSectionOffset;
		logger.debug("DataSectionOffset: " + header.dataSectionOffset);

		// Read SaltString
		header.salt = new byte[SALT_FIELD_LENGTH];
		virtualDiskFile.read(header.salt);
		String saltString = ByteUtil.bytArrayToHex(header.salt);
		logger.debug("Salt: " + saltString);

		// read Password Hash
		header.passwordHash = new byte[PASSWORDHASH_FIELD_LENGTH];
		virtualDiskFile.read(header.passwordHash);
		String passwordHashString = ByteUtil.bytArrayToHex(header.passwordHash);
		logger.debug("PasswordHash: " + passwordHashString);

		logger.debug("read Header Section DONE");
		return header;
	}

	public long getSectionSize() {
		return headerSectionSize;
	}

	public long getDataSectionOffset() {
		return dataSectionOffset;
	}

	public void close() {

	}

}
