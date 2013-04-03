package ch.eth.jcd.badgers.vfs.core.config;

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

	private String encryptionAlgorithm;

	private String compressionAlgorithm;

	/**
	 * the maximum size in bytes of the file we store our data on the host file system <br>
	 * values <=0 indicate that there is no limit <br>
	 * 
	 */
	private long maximumSize = -1;

	public String getCompressionAlgorithm() {
		return compressionAlgorithm;
	}

	public String getEncryptionAlgorithm() {
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

	public void setCompressionAlgorithm(String compressionAlgorithm) {
		this.compressionAlgorithm = compressionAlgorithm;
	}

	public void setEncryptionAlgorithm(String encryptionAlgorithm) {
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
