package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import java.io.File;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.VFSExporter;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

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
		new VFSExporter().exportFileOrFolder(entry, destination);
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
