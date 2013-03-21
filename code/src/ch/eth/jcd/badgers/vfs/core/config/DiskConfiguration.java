package ch.eth.jcd.badgers.vfs.core.config;

/**
 * 
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

	@Override
	public String toString() {
		return " Password=* EncryptionAlgorithm=" + encryptionAlgorithm + " CompressionAlgorithm=" + compressionAlgorithm + " HostFilePath=" + hostFilePath;
	}

	public String getHostFilePath() {
		return hostFilePath;
	}

	public void setHostFilePath(String hostFilePath) {
		this.hostFilePath = hostFilePath;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEncryptionAlgorithm() {
		return encryptionAlgorithm;
	}

	public void setEncryptionAlgorithm(String encryptionAlgorithm) {
		this.encryptionAlgorithm = encryptionAlgorithm;
	}

	public String getCompressionAlgorithm() {
		return compressionAlgorithm;
	}

	public void setCompressionAlgorithm(String compressionAlgorithm) {
		this.compressionAlgorithm = compressionAlgorithm;
	}

}
