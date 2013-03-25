package ch.eth.jcd.badgers.vfs.util;

import java.security.MessageDigest;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 
 * $Id
 * 
 * 
 */
public class HashUtil {

	public static byte[] hashSha512(byte[] toHash) {
		MessageDigest md = DigestUtils.getSha512Digest();
		return md.digest(toHash);
	}
}
