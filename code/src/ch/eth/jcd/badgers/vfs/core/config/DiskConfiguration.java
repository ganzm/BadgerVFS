package ch.eth.jcd.badgers.vfs.core.config;

import ch.eth.jcd.badgers.vfs.core.model.Compression;
import ch.eth.jcd.badgers.vfs.core.model.Encryption;

/**
 * DiskConfiguration used to open/create a VirtualDisk
 * 
 */
public class DiskConfiguration {

	/**
	 * Path where the virtual disk file is located on the host file system
	 */
	private String hostFilePath;

	private String password;

	private Encryption encryptionAlgorithm = Encryption.CAESAR;

	private Compression compressionAlgorithm = Compression.LZ77;

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

	public void setCompressionAlgorithm(Compression compressionAlgorithm) {
		this.compressionAlgorithm = compressionAlgorithm;
	}

	public void setEncryptionAlgorithm(Encryption encryptionAlgorithm) {
		this.encryptionAlgorithm = encryptionAlgorithm;
	}

	public void setHostFilePath(String hostFilePath) {
		this.hostFilePath = hostFilePath;
	}

	public void setMaximumSize(long maximumSize) {
		this.maximumSize = maximumSize;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return " Password=* EncryptionAlgorithm=" + encryptionAlgorithm + " CompressionAlgorithm=" + compressionAlgorithm + " MaxSize=" + maximumSize
				+ " HostFilePath=" + hostFilePath;
	}

}
