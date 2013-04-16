package ch.eth.jcd.badgers.vfs.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
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
	public void exportFileOrFolder(List<VFSEntry> entries, File destination) throws VFSException {
		Queue<ExportItem> queue = new LinkedList<ExportItem>();

		for (VFSEntry vfsEntry : entries) {
			queue.add(new ExportItem(vfsEntry, new File(destination, vfsEntry.getPath().getName())));
		}

		while (!queue.isEmpty()) {
			ExportItem nextItem = queue.remove();
			if (nextItem.getFrom().isDirectory()) {
				nextItem.getTo().mkdirs();
				for (VFSEntry e : nextItem.getFrom().getChildren()) {
					queue.add(new ExportItem(e, new File(nextItem.getTo(), e.getPath().getName())));
				}
			} else {
				LOGGER.debug("Exporting! source=" + nextItem.getFrom().getPath().toString() + " destination=" + nextItem.getTo().getAbsolutePath());
				InputStream is = nextItem.getFrom().getInputStream();
				OutputStream os;
				try {
					os = new FileOutputStream(nextItem.getTo());
					ChannelUtil.fastStreamCopy(is, os);
				} catch (IOException e) {
					LOGGER.error("ERROR while exporting: source=" + nextItem.getFrom().getPath().toString() + " destination="
							+ nextItem.getTo().getAbsolutePath());
				}
			}
		}
	}
}
