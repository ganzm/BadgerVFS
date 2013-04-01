package ch.eth.jcd.badgers.vfs.test.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.eth.jcd.badgers.vfs.compression.BadgersLZ77CompressionInputStream;
import ch.eth.jcd.badgers.vfs.compression.BadgersLZ77CompressionOutputStream;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;

public class BadgersLZ77CompressionTest {

	@BeforeClass
	public static void beforeClass() {
		UnittestLogger.init();
	}

	@Test
	public void testMaximumCompression() throws IOException {

		byte[] rawData = new byte[2048];
		System.out.println(rawData.toString());
		byte[] rawDataCopy = rawData.clone();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		BadgersLZ77CompressionOutputStream out = new BadgersLZ77CompressionOutputStream(outputStream);
		out.write(rawData);
		out.close();

		byte[] encrypted = outputStream.toByteArray();
		System.out.println("Byte length before encryption: " + rawData.length);
		System.out.println("Byte length after encryption:  " + encrypted.length);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(encrypted);

		BadgersLZ77CompressionInputStream in = new BadgersLZ77CompressionInputStream(inputStream);
		Assert.assertEquals(rawData.length, in.read(rawDataCopy));
		in.close();

		for (int i = 0; i < rawData.length; i++) {
			Assert.assertEquals(rawData[i], rawDataCopy[i]);
		}

	}

	@Test
	public void testRandomInput() throws IOException {

		byte[] rawData = new byte[2048];
		// byte[] rawData = new byte[] { -1, -1, -2 };
		byte[] rawDataCopy = new byte[2048];// = new byte[2048];
		new Random().nextBytes(rawData);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		BadgersLZ77CompressionOutputStream out = new BadgersLZ77CompressionOutputStream(outputStream);
		out.write(rawData);
		out.close();

		byte[] encrypted = outputStream.toByteArray();
		System.out.println("Byte length before encryption: " + rawData.length);
		System.out.println("Byte length after encryption:  " + encrypted.length);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(encrypted);

		BadgersLZ77CompressionInputStream in = new BadgersLZ77CompressionInputStream(inputStream);
		in.read(rawDataCopy);
		in.close();
		Assert.assertEquals(rawData.length, rawDataCopy.length);

		for (int i = 0; i < rawData.length; i++) {
			Assert.assertEquals("Expected equal data, is not equal at " + i, rawData[i], rawDataCopy[i]);
		}

	}

	@Test
	public void testStringInput() throws IOException {

		String stringDatas = "Miss Kalissippi from Mississippi is a " + "cowgirl who yells yippi when she rides her horse in "
				+ "the horse show in Mississippi.";
		byte[] rawDataCopy = new byte[2048];

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		BadgersLZ77CompressionOutputStream out = new BadgersLZ77CompressionOutputStream(outputStream);
		out.write(stringDatas.getBytes());
		out.close();

		byte[] encrypted = outputStream.toByteArray();
		System.out.println("Byte length before encryption: " + stringDatas.length());
		System.out.println("Byte length after encryption:  " + encrypted.length);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(encrypted);

		BadgersLZ77CompressionInputStream in = new BadgersLZ77CompressionInputStream(inputStream);
		Assert.assertEquals(stringDatas.length(), in.read(rawDataCopy));
		in.close();

		byte[] rawData = stringDatas.getBytes();
		for (int i = 0; i < rawData.length; i++) {
			Assert.assertEquals("Expected equal data, is not equal at " + i, rawData[i], rawDataCopy[i]);
		}

	}
}
