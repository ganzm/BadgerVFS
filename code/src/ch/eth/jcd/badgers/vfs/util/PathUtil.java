package ch.eth.jcd.badgers.vfs.util;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;

public class PathUtil {

	public static String concatPathAndFileName(String path, String fileName) {
		String result = null;
		if (VFSPath.FILE_SEPARATOR.equals(path)) {
			// import into root folder
			result = VFSPath.FILE_SEPARATOR + fileName;
		} else {
			result = path + VFSPath.FILE_SEPARATOR + fileName;
		}

		return result;
	}
}
