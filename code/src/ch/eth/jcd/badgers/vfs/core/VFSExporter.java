package ch.eth.jcd.badgers.vfs.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.util.ChannelUtil;

public class VFSExporter {
	private static final Logger LOGGER = Logger.getLogger(VFSExporter.class);

	private final class ExportItem {
		public VFSEntry getFrom() {
			return from;
		}

		public File getTo() {
			return to;
		}

		private final VFSEntry from;
		private final File to;

		private ExportItem(VFSEntry from, File to) {
			this.from = from;
			this.to = to;
		}
	}

	/**
	 * iterative tree traversal
	 * 
	 * @param path
	 * @param destination
	 */
	public void exportFileOrFolder(VFSPath path, File destination) throws VFSException {
		Queue<ExportItem> queue = new LinkedList<ExportItem>();

		queue.add(new ExportItem(path.getVFSEntry(), destination));
		while (!queue.isEmpty()) {
			ExportItem item = queue.remove();
			if (item.getFrom().isDirectory()) {
				item.getTo().mkdirs();
				for (VFSEntry e : item.getFrom().getChildren()) {
					queue.add(new ExportItem(e, new File(item.getTo(), e.getPath().getName())));
				}
			} else {
				LOGGER.debug("Exporting! source=" + item.getFrom().getPath().toString() + " destination=" + item.getTo().getAbsolutePath());
				InputStream is = item.getFrom().getInputStream();
				OutputStream os;
				try {
					os = new FileOutputStream(item.getTo());
					ChannelUtil.fastStreamCopy(is, os);
				} catch (IOException e) {
					LOGGER.error("ERROR while exporting: source=" + item.getFrom().getPath().toString() + " destination=" + item.getTo().getAbsolutePath());
				}
			}
		}
	}
}
