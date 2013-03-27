package ch.eth.jcd.badgers.vfs.test.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.encryption.CaesarInputStream;
import ch.eth.jcd.badgers.vfs.encryption.CaesarOutputStream;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;

public class EncryptionTest {

	@BeforeClass
	public static void beforeClass() {
		UnittestLogger.init();
	}

	@Test
	public void testCaesar() throws IOException {

		byte[] rawData = new byte[2048];
		byte[] rawDataCopy = new byte[2049];
		new Random().nextBytes(rawData);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		CaesarOutputStream out = new CaesarOutputStream(outputStream, 3);
		out.write(rawData);
		out.close();

		byte[] encrypted = outputStream.toByteArray();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(encrypted);

		CaesarInputStream in = new CaesarInputStream(inputStream, 3);
		Assert.assertEquals(rawData.length, in.read(rawDataCopy));
		in.close();

		for (int i = 0; i < rawData.length; i++) {
			Assert.assertEquals(rawData[i], rawDataCopy[i]);
		}

	}
}
