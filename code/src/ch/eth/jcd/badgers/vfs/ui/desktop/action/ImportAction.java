package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.VFSImporter;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;
import ch.eth.jcd.badgers.vfs.util.Pair;

public class ImportAction extends BadgerAction {

	private static final Logger LOGGER = Logger.getLogger(ImportAction.class);

	private final List<Pair<String, String>> host2DestinationPathList;

	private String currentHostFsSourcePath = "";

	private VFSImporter importer = new VFSImporter();

	public ImportAction(ActionObserver actionObserver, String hostFsSourcePath, String destinationPath) {
		super(actionObserver);
		this.host2DestinationPathList = new ArrayList<>();
		this.host2DestinationPathList.add(new Pair<String, String>(hostFsSourcePath, destinationPath));
	}

	public ImportAction(DesktopController actionObserver, List<Pair<String, String>> host2DestinationPathList) {
		super(actionObserver);
		this.host2DestinationPathList = host2DestinationPathList;
	}

	@Override
	public void runDiskAction(VFSDiskManager diskManager) throws VFSException {
		for (Pair<String, String> pathPair : host2DestinationPathList) {
			currentHostFsSourcePath = pathPair.getFirst();
			String destinationPath = pathPair.getSecond();

			LOGGER.info("Importing " + currentHostFsSourcePath);
			VFSPath path = diskManager.createPath(destinationPath);
			importer.importFileOrFolder(currentHostFsSourcePath, path);
		}
	}

	@Override
	public String getActionName() {
		return "Importing (" + importer.getEntriesDone() + "/" + importer.getTotalEntries() + ") " + currentHostFsSourcePath;
	}

	@Override
	public boolean isProgressIndicationSupported() {
		return true;
	}

	@Override
	public int getMaxProgress() {
		return importer.getTotalEntries();
	}

	@Override
	public int getCurrentProgress() {
		return importer.getEntriesDone();
	}
}
