package ch.eth.jcd.badgers.vfs.util;

import java.util.Arrays;

public class SecurityUtil {

	/**
	 * TODO anyone Apply crypto hash to a string and create a bytearray with a specific lenght
	 * 
	 * (truncate or add zeroes)
	 * 
	 * @param string
	 * @param targetLenght
	 * @return
	 */
	public static byte[] hashString(String string, int targetLenght) {
		return Arrays.copyOf("TODO".getBytes(), targetLenght);
	}
}
