package ch.eth.jcd.badgers.vfs.util;

import java.nio.ByteBuffer;

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

	public static byte[] longToBytes(long l) {
		return ByteBuffer.allocate(8).putLong(l).array();
	}

	public static long bytesToLong(byte[] bytes) {
		assert bytes.length == 8;

		ByteBuffer buf = ByteBuffer.wrap(bytes);
		return buf.getLong();
	}
}
