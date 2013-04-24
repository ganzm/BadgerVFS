package ch.eth.jcd.badgers.vfs.ui.desktop.action.disk;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.VFSImporter;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;
import ch.eth.jcd.badgers.vfs.util.Pair;

public class ImportAction extends DiskAction {

	private static final Logger LOGGER = Logger.getLogger(ImportAction.class);

	private final List<Pair<String, String>> host2DestinationPathList;

	private String currentHostFsSourcePath = "";

	private final VFSImporter importer = new VFSImporter();

	public ImportAction(final ActionObserver actionObserver, final String hostFsSourcePath, final String destinationPath) {
		super(actionObserver);
		this.host2DestinationPathList = new ArrayList<>();
		this.host2DestinationPathList.add(new Pair<String, String>(hostFsSourcePath, destinationPath));
	}

	public ImportAction(final DesktopController actionObserver, final List<Pair<String, String>> host2DestinationPathList) {
		super(actionObserver);
		this.host2DestinationPathList = host2DestinationPathList;
	}

	@Override
	public void runDiskAction(final VFSDiskManager diskManager) throws VFSException {
		for (final Pair<String, String> pathPair : host2DestinationPathList) {
			currentHostFsSourcePath = pathPair.getFirst();
			final String destinationPath = pathPair.getSecond();

			LOGGER.info("Importing " + currentHostFsSourcePath);
			final VFSPath path = diskManager.createPath(destinationPath);
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
