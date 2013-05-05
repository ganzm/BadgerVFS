package ch.eth.jcd.badgers.vfs.core.config;

import java.io.Serializable;
import java.util.UUID;

import ch.eth.jcd.badgers.vfs.core.model.Compression;
import ch.eth.jcd.badgers.vfs.core.model.Encryption;

/**
 * DiskConfiguration used to open/create a VirtualDisk
 * 
 */
public class DiskConfiguration implements Serializable {

	private static final long serialVersionUID = -146786101445454171L;

	/**
	 * Path where the virtual disk file is located on the host file system
	 */
	private String hostFilePath;

	private String password;

	private Encryption encryptionAlgorithm = Encryption.CAESAR;

	private Compression compressionAlgorithm = Compression.LZ77;

	private String linkedHostName;

	private UUID diskId;

	/**
	 * If set to true this is a VirtualDisk located on the SynchronisationServer
	 */
	private boolean syncServerMode = false;

	/**
	 * the maximum size in bytes of the file we store our data on the host file system <br>
	 * values <=0 indicate that there is no limit <br>
	 * 
	 */
	private long maximumSize = -1;

	public Compression getCompressionAlgorithm() {
		return compressionAlgorithm;
	}

	public Encryption getEncryptionAlgorithm() {
		return encryptionAlgorithm;
	}

	public String getHostFilePath() {
		return hostFilePath;
	}

	public long getMaximumSize() {
		return maximumSize;
	}

	public String getPassword() {
		return password;
	}

	public void setCompressionAlgorithm(final Compression compressionAlgorithm) {
		this.compressionAlgorithm = compressionAlgorithm;
	}

	public void setEncryptionAlgorithm(final Encryption encryptionAlgorithm) {
		this.encryptionAlgorithm = encryptionAlgorithm;
	}

	public void setHostFilePath(final String hostFilePath) {
		this.hostFilePath = hostFilePath;
	}

	public void setMaximumSize(final long maximumSize) {
		this.maximumSize = maximumSize;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public String getLinkedHostName() {
		return linkedHostName;
	}

	public boolean isHostNameLinked() {
		return linkedHostName != null && "".equals(linkedHostName) == false;
	}

	public void setLinkedHostName(final String linkedHostName) {
		this.linkedHostName = linkedHostName;
	}

	@Override
	public String toString() {
		return " Password=* EncryptionAlgorithm=" + encryptionAlgorithm + " CompressionAlgorithm=" + compressionAlgorithm + " MaxSize=" + maximumSize
				+ " HostFilePath=" + hostFilePath;
	}

	public boolean isSyncServerMode() {
		return syncServerMode;
	}

	public void setSyncServerMode(boolean syncServerMode) {
		this.syncServerMode = syncServerMode;
	}

	public void setDiskId(UUID diskId) {
		this.diskId = diskId;
	}

	public UUID getDiskId() {
		return this.diskId;
	}
}
