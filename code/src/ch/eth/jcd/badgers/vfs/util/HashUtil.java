package ch.eth.jcd.badgers.vfs.util;

import java.nio.charset.Charset;
import java.security.MessageDigest;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 
 * $Id
 * 
 * 
 */
public class HashUtil {

	public static final Charset cs = Charset.forName("UTF8");

	public static byte[] hashUtf8String(String string) {
		return hashSha512(string.getBytes(cs));
	}

	public static byte[] hashSha512(byte[] toHash) {
		MessageDigest md = DigestUtils.getSha512Digest();
		return md.digest(toHash);
	}
}
