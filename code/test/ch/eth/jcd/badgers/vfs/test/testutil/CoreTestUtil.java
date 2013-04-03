package ch.eth.jcd.badgers.vfs.test.testutil;

import org.junit.Assert;

import ch.eth.jcd.badgers.vfs.core.VFSDirectoryImpl;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;

public class CoreTestUtil {

	public static void printDirBTree(VFSEntry directory) {
		if (directory instanceof VFSDirectoryImpl) {
			((VFSDirectoryImpl) directory).debugPrint();

			StringBuffer buf = new StringBuffer();
			boolean result = ((VFSDirectoryImpl) directory).performTreeSanityCheck(buf);
			Assert.assertTrue(buf.toString(), result);
		}
	}
}
