package ch.eth.jcd.badgers.vfs.test.testutil;

import org.apache.log4j.Logger;
import org.junit.Assert;

import ch.eth.jcd.badgers.vfs.core.VFSDirectoryImpl;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class CoreTestUtil {
	private static final Logger LOGGER = Logger.getLogger(CoreTestUtil.class);

	public static void printDirBTree(VFSEntry directory) {
		try {

			if (directory instanceof VFSDirectoryImpl) {
				((VFSDirectoryImpl) directory).debugPrint();

				StringBuffer buf = new StringBuffer();
				boolean result = ((VFSDirectoryImpl) directory).performTreeSanityCheck(buf);
				Assert.assertTrue(buf.toString(), result);
			}
		} catch (VFSException e) {
			LOGGER.error("", e);
			Assert.fail(e.getMessage());
		}
	}
}
