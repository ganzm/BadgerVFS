package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import java.io.File;
import java.util.List;

import javax.swing.JFrame;

import ch.eth.jcd.badgers.vfs.core.VFSExporter;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.exception.VFSException;

public class ExportAction extends BadgerAction {
	private final List<VFSEntry> entries;
	private final File destination;

	private final JFrame desktopFrame;

	public ExportAction(ActionObserver actionObserver, List<VFSEntry> entriesToExport, File destination, JFrame desktopFrame) {
		super(actionObserver);
		this.entries = entriesToExport;
		this.destination = destination;
		this.desktopFrame = desktopFrame;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		new VFSExporter().exportFileOrFolder(entries, destination);
	}

	public File getDestination() {
		return destination;
	}

	public List<VFSEntry> getEntries() {
		return entries;
	}

	public JFrame getDesktopFrame() {
		return desktopFrame;
	}

}
