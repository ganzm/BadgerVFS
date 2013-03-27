package ch.eth.jcd.badgers.vfs.test.core;

import java.util.Random;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;
import ch.eth.jcd.badgers.vfs.util.HashUtil;

public class HashTest {

	@BeforeClass
	public static void beforeClass() {
		UnittestLogger.init();
	}

	@Test
	public void testShaHash() {

		byte[] data = new byte[Math.abs(new Random().nextInt()) % 10000];
		new Random().nextBytes(data);

		byte[] hashedData = HashUtil.hashSha512(data);
		Assert.assertEquals(64, hashedData.length);
	}
}
