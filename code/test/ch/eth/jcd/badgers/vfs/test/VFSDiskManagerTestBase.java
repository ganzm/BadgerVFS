package ch.eth.jcd.badgers.vfs.test;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import ch.eth.jcd.badgers.vfs.core.VFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.mock.MockedVFSDiskManagerImpl;
import ch.eth.jcd.badgers.vfs.test.testutil.UnittestLogger;

public class VFSDiskManagerTestBase {

	protected static VFSDiskManager diskManager;

	@BeforeClass
	public static void beforeClass() throws VFSException {
		UnittestLogger.init();

		diskManager = setupDefault("test.tmp", null);
	}

	@Before
	public void before() throws VFSException {
	}

	@AfterClass
	public static void afterClass() throws VFSException {
		diskManager.dispose();
	}

	protected static void teardownMock() {

	}

	protected static VFSDiskManager setupMock() throws VFSException {
		return MockedVFSDiskManagerImpl.create(getMockedConfig());
	}

	protected static DiskConfiguration getMockedConfig() {
		DiskConfiguration config = new DiskConfiguration();
		config.setHostFilePath(getMockRootDir());
		return config;

	}

	private static String getMockRootDir() {
		String tempDir = System.getProperty("java.io.tmpdir");
		String fileName;
		if (tempDir.endsWith("/") || tempDir.endsWith("\\")) {
			fileName = tempDir + "mocked";
		} else {
			fileName = tempDir + File.separatorChar + "mocked";
		}
		return fileName;

	}

	public static VFSDiskManager setupDefault(String name, String linkedHostName) throws VFSException {

		String tempDir = System.getProperty("java.io.tmpdir");

		// Delete if this file already exists
		String fileName = tempDir + File.separatorChar + name;
		if (new File(fileName).exists()) {
			new File(fileName).delete();
		}
		DiskConfiguration config = new DiskConfiguration();
		config.setHostFilePath(fileName);
		config.setLinkedHostName(linkedHostName);

		return VFSDiskManagerImpl.create(config);

	}

	public void teardownDefault() throws VFSException {
		diskManager.dispose();
	}

}
