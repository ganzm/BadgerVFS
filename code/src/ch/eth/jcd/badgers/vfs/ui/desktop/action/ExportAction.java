package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.util.ChannelUtil;

public class ExportAction extends BadgerAction {
	private static final Logger LOGGER = Logger.getLogger(ExportAction.class);

	private final VFSEntry entry;
	private final File destination;

	private final JFrame desktopFrame;

	public ExportAction(ActionObserver actionObserver, VFSEntry entryToExport, File destination, JFrame desktopFrame) {
		super(actionObserver);
		LOGGER.debug("ExportAction created: source=" + entryToExport.getPath().toString() + " destination=" + destination.getAbsolutePath());
		this.entry = entryToExport;
		this.destination = destination;
		this.desktopFrame = desktopFrame;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {

		LOGGER.debug("Exporting! source=" + entry.getPath().toString() + " destination=" + destination.getAbsolutePath());
		InputStream is = entry.getInputStream();
		OutputStream os;
		try {
			os = new FileOutputStream(destination);
			ChannelUtil.fastStreamCopy(is, os);
		} catch (IOException e) {
			LOGGER.error("ERROR while exportin: source=" + entry.getPath().toString() + " destination=" + destination.getAbsolutePath());
		}

	}

	public File getDestination() {
		return destination;
	}

	public VFSEntry getEntry() {
		return entry;
	}

	public JFrame getDesktopFrame() {
		return desktopFrame;
	}

}
