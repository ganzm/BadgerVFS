package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.VFSImporter;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;

public class ImportAction extends BadgerAction {

	private static final Logger LOGGER = Logger.getLogger(ImportAction.class);

	private final List<String> hostFsSourcePathes;
	private final String destinationPath;

	public ImportAction(ActionObserver actionObserver, String hostFsSourcePath, String destinationPath) {
		super(actionObserver);
		this.destinationPath = destinationPath;
		this.hostFsSourcePathes = new ArrayList<>();
		this.hostFsSourcePathes.add(hostFsSourcePath);
	}

	public ImportAction(DesktopController actionObserver, List<String> hostFsSourcePathes, String destinationPath) {
		super(actionObserver);
		this.hostFsSourcePathes = hostFsSourcePathes;
		this.destinationPath = destinationPath;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		for (String hostFsSourcePath : hostFsSourcePathes) {
			LOGGER.info("Importing " + hostFsSourcePath);
			VFSPath path = diskManager.createPath(destinationPath);
			VFSImporter importer = new VFSImporter();
			importer.importFileOrFolder(hostFsSourcePath, path);
		}
	}
}
