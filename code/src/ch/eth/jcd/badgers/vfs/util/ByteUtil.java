package ch.eth.jcd.badgers.vfs.util;

public class ByteUtil {

	/**
	 * 
	 * {@link http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java}
	 * 
	 * @param a
	 * @return
	 */
	public static String bytArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder();
		for (byte b : a)
			sb.append(String.format("%02x", b & 0xff));
		return sb.toString();
	}

}
