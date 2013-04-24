package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import java.io.File;
import java.util.List;

import javax.swing.JFrame;

import ch.eth.jcd.badgers.vfs.core.VFSExporter;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;

public class ExportAction extends DiskAction {
	private final List<VFSEntry> entries;
	private final File destination;

	private final JFrame desktopFrame;

	private final VFSExporter exporter = new VFSExporter();

	/**
	 * 
	 * @param actionObserver
	 * @param entriesToExport
	 * @param destinationFolder
	 *            folder on the host operating system
	 * @param desktopFrame
	 */
	public ExportAction(ActionObserver actionObserver, List<VFSEntry> entriesToExport, File destinationFolder, JFrame desktopFrame) {
		super(actionObserver);
		this.entries = entriesToExport;
		this.destination = destinationFolder;
		this.desktopFrame = desktopFrame;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		exporter.exportFileOrFolder(entries, destination);
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

	@Override
	public String getActionName() {
		return "Exporting (" + exporter.getEntriesDone() + "/" + exporter.getTotalEntries() + ") ";
	}

	@Override
	public boolean isProgressIndicationSupported() {
		return true;
	}

	@Override
	public int getMaxProgress() {
		return exporter.getTotalEntries();
	}

	@Override
	public int getCurrentProgress() {
		return exporter.getEntriesDone();
	}

}
