package ch.eth.jcd.badgers.vfs.core.header;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.directory.DirectoryBlock;
import ch.eth.jcd.badgers.vfs.core.directory.DirectorySectionHandler;
import ch.eth.jcd.badgers.vfs.core.model.Compression;
import ch.eth.jcd.badgers.vfs.core.model.Encryption;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.util.ByteUtil;
import ch.eth.jcd.badgers.vfs.util.SecurityUtil;

public final class HeaderSectionHandler {
	private static final Logger LOGGER = Logger.getLogger(HeaderSectionHandler.class);

	private static Charset cs = Charset.forName("ASCII");

	private static final int INFO_FIELD_LENGTH = 50;
	private static final int VERSION_FIELD_LENGTH = 10;
	private static final int COMPRESSION_FIELD_LENGTH = 20;
	private static final int USERNAME_FIELD_LENGTH = 20;
	private static final int ENCRYPTION_FIELD_LENGTH = 20;
	private static final int SALT_FIELD_LENGTH = 20;
	private static final int PASSWORDHASH_FIELD_LENGTH = 20;
	private static final int LINKEDHOSTNAME_FIELD_LENGTH = 200;

	private static String defaultInfoString = "Badger VFS 2013 V1.0";
	private static String defaultVersionString = "0.1";
	private static String defaultUserName = "user";

	private String infoString;

	private String versionString;

	private UUID uuid;

	private long maximumSize;

	private long headerSectionSize;

	private String compressionString;

	private String encryptionString;

	private byte[] salt;

	private String userName;

	private byte[] passwordHash;

	private String linkedHostName;

	private long directorySectionOffset;

	private long dataSectionOffset;

	private long linkedHostNamePosition;

	/**
	 * Start position in the file where we write linkedHostVersion
	 */
	private long linkedHostVersionPosition;

	/**
	 * Version number we have last seen on the synchronization server
	 */
	private long linkedHostVersion = -1;

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

		// write uuid
		header.uuid = UUID.randomUUID();
		virtualDiskFile.writeLong(header.uuid.getLeastSignificantBits());
		virtualDiskFile.writeLong(header.uuid.getMostSignificantBits());

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

		// write userName
		header.userName = defaultUserName;
		virtualDiskFile.write(Arrays.copyOf(header.userName.getBytes(cs), USERNAME_FIELD_LENGTH));

		// write Password Hash
		header.passwordHash = SecurityUtil.hashString(config.getPassword(), PASSWORDHASH_FIELD_LENGTH);
		virtualDiskFile.write(header.passwordHash);

		// write SaltString
		header.salt = new byte[SALT_FIELD_LENGTH];
		new Random().nextBytes(header.salt);
		virtualDiskFile.write(header.salt);

		// write linkedHostName
		header.linkedHostNamePosition = virtualDiskFile.getFilePointer();
		header.linkedHostName = config.getLinkedHostName();
		if (header.linkedHostName == null) {
			virtualDiskFile.write(Arrays.copyOf(new byte[0], LINKEDHOSTNAME_FIELD_LENGTH));
		} else {
			virtualDiskFile.write(Arrays.copyOf(header.linkedHostName.getBytes(cs), LINKEDHOSTNAME_FIELD_LENGTH));
		}

		// write Version number seen from linked synchronization server
		header.linkedHostVersionPosition = virtualDiskFile.getFilePointer();
		virtualDiskFile.writeLong(header.linkedHostVersion);

		// we are done writing header data
		// get current location
		header.directorySectionOffset = virtualDiskFile.getFilePointer();

		// now we are at the end of the directory section, remember this position
		header.headerSectionSize = header.directorySectionOffset;

		virtualDiskFile.seek(indexSectionOffsetIndicatorLocation);

		// write IndexSectionOffset | double (8 byte)
		virtualDiskFile.writeLong(header.directorySectionOffset);

		// DataSectionOffset | double (8 byte)
		header.dataSectionOffset = header.directorySectionOffset + estimateDirectorySectionSize(config.getMaximumSize());
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

		// read uuid
		long leastSigBits = virtualDiskFile.readLong();
		long mostSigBits = virtualDiskFile.readLong();
		header.uuid = new UUID(mostSigBits, leastSigBits);
		LOGGER.debug("UUID: " + header.uuid);

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

		// read user name
		byte[] userNameBytes = new byte[USERNAME_FIELD_LENGTH];
		virtualDiskFile.read(userNameBytes);
		header.userName = new String(userNameBytes, cs).trim();
		LOGGER.debug("Username: " + header.userName);

		// read Password Hash
		header.passwordHash = new byte[PASSWORDHASH_FIELD_LENGTH];
		virtualDiskFile.read(header.passwordHash);
		String passwordHashString = ByteUtil.bytArrayToHex(header.passwordHash);
		LOGGER.debug("PasswordHash: " + passwordHashString);

		// Read SaltString
		header.salt = new byte[SALT_FIELD_LENGTH];
		virtualDiskFile.read(header.salt);
		String saltString = ByteUtil.bytArrayToHex(header.salt);
		LOGGER.debug("Salt: " + saltString);

		// read linkedHostName
		header.linkedHostNamePosition = virtualDiskFile.getFilePointer();
		byte[] linkedHostNameBytes = new byte[LINKEDHOSTNAME_FIELD_LENGTH];
		virtualDiskFile.read(linkedHostNameBytes);
		header.linkedHostName = new String(linkedHostNameBytes, cs).trim();
		config.setLinkedHostName(header.linkedHostName);
		LOGGER.debug("Linked Host: " + header.linkedHostName);

		// read Version number from linked host name
		header.linkedHostVersionPosition = virtualDiskFile.getFilePointer();
		header.linkedHostVersion = virtualDiskFile.readLong();

		LOGGER.debug("read Header Section DONE");
		return header;
	}

	public long getSectionSize() {
		return headerSectionSize;
	}

	public long getDataSectionOffset() {
		return dataSectionOffset;
	}

	/**
	 * scale directory section with maximum disk size
	 * 
	 * TODO this method may be improved.
	 * 
	 * @param maxDiskSize
	 * @return
	 */
	private static long estimateDirectorySectionSize(long maxDiskSize) {

		long estimated = maxDiskSize / 1024 / 1024 * 100;
		estimated = DirectoryBlock.BLOCK_SIZE * estimated;
		return Math.max(estimated, DirectorySectionHandler.DEFAULT_DIRECTORYSECTION_SIZE);
	}

	public void close() {

	}

	public void setLinkedHostName(RandomAccessFile virtualDiskFile, String hostName) throws VFSException {
		if (linkedHostName != null && !linkedHostName.isEmpty()) {
			throw new VFSException("Disk is already linked to [" + hostName + "]");
		}

		try {
			this.linkedHostName = hostName;

			virtualDiskFile.seek(linkedHostNamePosition);
			virtualDiskFile.write(Arrays.copyOf(hostName.getBytes(cs), LINKEDHOSTNAME_FIELD_LENGTH));

			LOGGER.info("Disk now linked to " + hostName);
		} catch (IOException e) {
			throw new VFSException("", e);
		}
	}

	public void setlinkedHostVersion(RandomAccessFile virtualDiskFile, long serverVersion) throws VFSException {
		this.linkedHostVersion = serverVersion;
		try {
			virtualDiskFile.seek(linkedHostVersionPosition);
			virtualDiskFile.writeLong(serverVersion);
		} catch (IOException e) {
			throw new VFSException("", e);
		}
	}

	public long getLinkedHostVersion() {
		return linkedHostVersion;
	}

	public UUID getUuid() {
		return uuid;
	}
}
