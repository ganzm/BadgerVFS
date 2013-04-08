package ch.eth.jcd.badgers.vfs.test.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.compression.BadgersLZ77CompressionInputStream;
import ch.eth.jcd.badgers.vfs.compression.BadgersLZ77CompressionOutputStream;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;

public class BadgersLZ77CompressionTest {
	private static final Logger LOGGER = Logger.getLogger(BadgersLZ77CompressionTest.class);

	@BeforeClass
	public static void beforeClass() {
		UnittestLogger.init();
	}

	@Test
	public void testMaximumCompression() throws IOException {
		byte[] rawData = new byte[2048];
		testCompress(rawData);
	}

	@Test
	public void testRandomInput() throws IOException {
		Random rnd = new Random();
		byte[] rawData = new byte[rnd.nextInt(100000)];
		rnd.nextBytes(rawData);

		testCompress(rawData);
	}

	@Test
	public void testStringInput() throws IOException {
		String stringData = "Miss Kalissippi from Mississippi is a " + "cowgirl who yells yippi when she rides her horse in "
				+ "the horse show in Mississippi.";

		testCompress(stringData.getBytes());
	}

	private void testCompress(byte[] rawData) throws IOException {
		byte[] rawDataCopy = new byte[rawData.length];

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		BadgersLZ77CompressionOutputStream out = new BadgersLZ77CompressionOutputStream(outputStream);
		out.write(rawData);
		out.flush();
		out.close();

		byte[] encrypted = outputStream.toByteArray();
		LOGGER.debug("Byte length before encryption: " + rawData.length);
		LOGGER.debug("Byte length after encryption:  " + encrypted.length);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(encrypted);

		BadgersLZ77CompressionInputStream in = new BadgersLZ77CompressionInputStream(inputStream);
		in.read(rawDataCopy);

		Assert.assertEquals(-1, in.read());

		in.close();
		Assert.assertEquals(rawData.length, rawDataCopy.length);

		for (int i = 0; i < rawData.length; i++) {
			Assert.assertEquals("Expected equal data, is not equal at " + i, rawData[i], rawDataCopy[i]);
		}
	}
}
