package ch.eth.jcd.badgers.vfs.core.header;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.directory.DirectorySectionHandler;
import ch.eth.jcd.badgers.vfs.core.model.Compression;
import ch.eth.jcd.badgers.vfs.core.model.Encryption;
import ch.eth.jcd.badgers.vfs.util.ByteUtil;
import ch.eth.jcd.badgers.vfs.util.SecurityUtil;

public final class HeaderSectionHandler {
	private static final Logger LOGGER = Logger.getLogger(HeaderSectionHandler.class);

	private static Charset cs = Charset.forName("ASCII");

	private static final int INFO_FIELD_LENGTH = 50;
	private static final int VERSION_FIELD_LENGTH = 10;
	private static final int COMPRESSION_FIELD_LENGTH = 20;
	private static final int ENCRYPTION_FIELD_LENGTH = 20;
	private static final int SALT_FIELD_LENGTH = 20;
	private static final int PASSWORDHASH_FIELD_LENGTH = 20;

	private static String defaultInfoString = "Badger VFS 2013 V1.0";
	private static String defaultVersionString = "0.1";

	private String infoString;

	private String versionString;

	private long maximumSize;

	private long headerSectionSize;

	private String compressionString;

	private String encryptionString;

	private byte[] salt;

	private byte[] passwordHash;

	private long directorySectionOffset;

	private long dataSectionOffset;

	private HeaderSectionHandler() {
	}

	public static HeaderSectionHandler createNew(RandomAccessFile virtualDiskFile, DiskConfiguration config) throws IOException {

		LOGGER.debug("init Header Section...");

		HeaderSectionHandler header = new HeaderSectionHandler();

		// go to start of the header section
		virtualDiskFile.seek(0);

		// write Info String
		header.infoString = defaultInfoString;
		virtualDiskFile.write(Arrays.copyOf(header.infoString.getBytes(cs), INFO_FIELD_LENGTH));

		// write Version String
		header.versionString = defaultVersionString;
		virtualDiskFile.write(Arrays.copyOf(header.versionString.getBytes(cs), VERSION_FIELD_LENGTH));

		// Write maximum size of this virtual disk file
		header.maximumSize = config.getMaximumSize();
		virtualDiskFile.writeLong(header.maximumSize);

		// Write compression Algorithm used
		virtualDiskFile.write(Arrays.copyOf(config.getCompressionAlgorithm().toString().getBytes(cs), COMPRESSION_FIELD_LENGTH));

		// Write encryption Algorithm used
		virtualDiskFile.write(Arrays.copyOf(config.getEncryptionAlgorithm().toString().getBytes(cs), ENCRYPTION_FIELD_LENGTH));

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
		header.headerSectionSize = header.directorySectionOffset;

		virtualDiskFile.seek(indexSectionOffsetIndicatorLocation);

		// write IndexSectionOffset | double (8 byte)
		virtualDiskFile.writeLong(header.directorySectionOffset);

		// DataSectionOffset | double (8 byte)
		header.dataSectionOffset = header.directorySectionOffset + DirectorySectionHandler.DEFAULT_DIRECTORYSECTION_SIZE;
		virtualDiskFile.writeLong(header.dataSectionOffset);

		virtualDiskFile.seek(header.directorySectionOffset);

		LOGGER.debug("init Header Section DONE");
		return header;

	}

	public static HeaderSectionHandler createExisting(RandomAccessFile virtualDiskFile, DiskConfiguration config) throws IOException {
		LOGGER.debug("reading Header Section...");

		HeaderSectionHandler header = new HeaderSectionHandler();

		Charset cs = Charset.forName("ASCII");

		// go to start of the index section
		virtualDiskFile.seek(0);

		// Read Info String
		byte[] infoBytes = new byte[INFO_FIELD_LENGTH];
		virtualDiskFile.read(infoBytes);
		header.infoString = new String(infoBytes, cs).trim();
		LOGGER.debug("Info: " + header.infoString);

		// Read Version String
		byte[] versionBytes = new byte[VERSION_FIELD_LENGTH];
		virtualDiskFile.read(versionBytes);
		header.versionString = new String(versionBytes, cs).trim();
		LOGGER.debug("Version: " + header.versionString);

		// Maximum Size
		header.maximumSize = virtualDiskFile.readLong();
		config.setMaximumSize(header.maximumSize);
		LOGGER.debug("MaximumSize: " + header.maximumSize);

		// Read compression
		byte[] compressionBytes = new byte[COMPRESSION_FIELD_LENGTH];
		virtualDiskFile.read(compressionBytes);
		header.compressionString = new String(compressionBytes, cs).trim();
		config.setCompressionAlgorithm(Compression.fromString(header.compressionString));
		LOGGER.debug("Compression: " + header.compressionString);

		// Read encryption
		byte[] encryptionBytes = new byte[ENCRYPTION_FIELD_LENGTH];
		virtualDiskFile.read(encryptionBytes);
		header.encryptionString = new String(encryptionBytes, cs).trim();
		config.setEncryptionAlgorithm(Encryption.fromString(header.encryptionString));
		LOGGER.debug("Encryption: " + header.encryptionString);

		// read 8 bytes - DirectorySectionOffset
		header.directorySectionOffset = virtualDiskFile.readLong();
		LOGGER.debug("DirectorySectionOffset: " + header.directorySectionOffset);

		// read 8 bytes - DataSectionOffset
		header.dataSectionOffset = virtualDiskFile.readLong();
		header.headerSectionSize = header.directorySectionOffset;
		LOGGER.debug("DataSectionOffset: " + header.dataSectionOffset);

		// Read SaltString
		header.salt = new byte[SALT_FIELD_LENGTH];
		virtualDiskFile.read(header.salt);
		String saltString = ByteUtil.bytArrayToHex(header.salt);
		LOGGER.debug("Salt: " + saltString);

		// read Password Hash
		header.passwordHash = new byte[PASSWORDHASH_FIELD_LENGTH];
		virtualDiskFile.read(header.passwordHash);
		String passwordHashString = ByteUtil.bytArrayToHex(header.passwordHash);
		LOGGER.debug("PasswordHash: " + passwordHashString);

		LOGGER.debug("read Header Section DONE");
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
