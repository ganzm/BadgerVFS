package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManagerFactory;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSEntry;
import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.sync.client.OfflineRemoteManager;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.CopyAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.CreateFolderAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.CutAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.DeleteEntryAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.ExportAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.GetFolderContentAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.ImportAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.OpenFileInFolderAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.RenameEntryAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.BadgerFileExtensionFilter;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryTableModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.ParentFolderEntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.RemoteSynchronisationWizardContext;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.RemoteSynchronisationWizardContext.LoginActionEnum;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.DiskSpaceDialog;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.ImportDialog;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.InfoDialog;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.LoginDialog;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.NewDiskCreationDialog;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.NewRemoteDiskCreationDialog;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.RemoteDiskDialog;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.ServerUrlDialog;
import ch.eth.jcd.badgers.vfs.util.Pair;
import ch.eth.jcd.badgers.vfs.util.PathUtil;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public class DesktopController extends BadgerController implements ActionObserver {
	public enum ClipboardAction {
		COPY, CUT
	}

	private static final Logger LOGGER = Logger.getLogger(DesktopController.class);

	private final EntryTableModel entryTableModel = new EntryTableModel();

	private VFSPath currentFolder;

	private Pair<ClipboardAction, List<EntryUiModel>> clipboard;

	private DiskWorkerController workerController = null;

	/**
	 * is null when there is no disk opened or if disk is not linked
	 */
	private RemoteManager remoteManager = null;

	public DesktopController(final BadgerViewBase desktopView) {
		super(desktopView);
	}

	/**
	 * shows a blocking dialog which lets you create a new VFS Disk
	 * 
	 * @param desktop
	 */
	public void openCreateNewDiskDialog(final JFrame desktop) {
		final NewDiskCreationDialog dialog = new NewDiskCreationDialog(desktop, this);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openCreateNewRemoteDiskDialog(final JFrame desktop, final RemoteSynchronisationWizardContext wizard) {
		final NewRemoteDiskCreationDialog dialog = new NewRemoteDiskCreationDialog(desktop, wizard);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openImportDialog(final JFrame desktop) {
		final ImportDialog dialog = new ImportDialog(this, currentFolder.getAbsolutePath());
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openDiskSpaceDialog(final JFrame desktop) {
		final DiskSpaceDialog dialog = new DiskSpaceDialog(desktop, this);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openInfoDialog(final JFrame desktop) {
		final InfoDialog dialog = new InfoDialog(desktop);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openLinkDiskDialog(final JFrame desktop) {
		final RemoteSynchronisationWizardContext ctx = new RemoteSynchronisationWizardContext(LoginActionEnum.SYNC);
		final ServerUrlDialog dialog = new ServerUrlDialog(desktop, ctx);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openConnectRemoteDialog(final JFrame desktop) {
		final RemoteSynchronisationWizardContext ctx = new RemoteSynchronisationWizardContext(LoginActionEnum.LOGIN);
		final ServerUrlDialog dialog = new ServerUrlDialog(desktop, ctx);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openLoginDialog(final JFrame desktop, final RemoteSynchronisationWizardContext wizardContext) {
		final LoginDialog dialog = new LoginDialog(desktop, wizardContext);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void startSyncToServer(final RemoteSynchronisationWizardContext wizardContext) {
		throw new UnsupportedOperationException("TODO");
	}

	public void openRemoteDiskDialog(final JFrame desktop, final RemoteSynchronisationWizardContext wizard) {
		final RemoteDiskDialog dialog = new RemoteDiskDialog(desktop, wizard);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	/**
	 * You have clicked on the Disk / Open menu item
	 * 
	 * @param parent
	 * @throws VFSException
	 */
	public void openFileChooserForDiskOpen(final Component parent) throws VFSException {
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle("Open Badger Disk");
		fileChooser.setMultiSelectionEnabled(false);
		final FileFilter bfsType = new BadgerFileExtensionFilter();
		fileChooser.addChoosableFileFilter(bfsType);
		fileChooser.setFileFilter(bfsType);
		fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

		final int returnVal = fileChooser.showOpenDialog(parent);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = fileChooser.getSelectedFile();
			openDisk(selectedFile.getAbsolutePath());
		}
	}

	/**
	 * Actualy open a disk You acknowledged the Disk / Open FileChooser Dialog
	 * 
	 * @param path
	 * @throws VFSException
	 */
	private void openDisk(final String path) throws VFSException {
		if (!isInManagementMode()) {
			throw new VFSException("Cannot open Disk on " + path + " - close current disk first");
		}

		LOGGER.debug("Opening Disk from Path " + path);

		final DiskConfiguration config = new DiskConfiguration();
		config.setHostFilePath(path);

		final VFSDiskManagerFactory factory = VFSDiskManagerFactory.getInstance();
		final VFSDiskManager diskManager = factory.openDiskManager(config);

		remoteManager = initRemoteManager(config);

		// create and start WorkerController
		workerController = new DiskWorkerController(diskManager);
		workerController.startWorkerController();

		loadRootFolder();

		updateGUI();
	}

	private RemoteManager initRemoteManager(final DiskConfiguration config) {
		final String hostLink = config.getLinkedHostName();
		if (hostLink == null || "".equals(hostLink)) {
			LOGGER.debug("Disk not linked");
			return new OfflineRemoteManager();
		}

		return new RemoteManager(hostLink);
	}

	public void createDisk(final DiskConfiguration config) throws VFSException {
		if (!isInManagementMode()) {
			throw new VFSException("Cannot create new Disk - close current disk first");
		}

		final VFSDiskManagerFactory factory = VFSDiskManagerFactory.getInstance();
		final VFSDiskManager diskManager = factory.createDiskManager(config);

		remoteManager = initRemoteManager(config);

		// create and start WorkerController
		workerController = new DiskWorkerController(diskManager);
		workerController.startWorkerController();

		loadRootFolder();

		updateGUI();
	}

	private void loadRootFolder() {
		final GetFolderContentAction getFolderContentAction = new GetFolderContentAction(this);
		workerController.enqueue(getFolderContentAction);
	}

	public boolean isInManagementMode() {
		return workerController == null;
	}

	/**
	 * You have clicked on the Disk / Close menu item
	 * 
	 * @param desktopFrame
	 * @throws VFSException
	 */
	public void closeDisk(final JFrame desktopFrame) throws VFSException {
		if (isInManagementMode()) {
			throw new VFSException("Cannot close new Disk - no current disk opened");
		}

		workerController.dispose();
		workerController = null;
		updateGUI();
	}

	@Override
	public void onActionFailed(final AbstractBadgerAction action, final Exception e) {
		SwingUtil.handleException(null, e);
		updateGUI();
	}

	@Override
	public void onActionFinished(final AbstractBadgerAction action) {
		if (action instanceof GetFolderContentAction) {
			getFolderContentActionFinished((GetFolderContentAction) action);
		} else if (action instanceof DeleteEntryAction) {
			// after deletion of multiple items we cannot operate anymore with table indices
			final GetFolderContentAction reloadCurrentFolderAction = new GetFolderContentAction(this, currentFolder);
			workerController.enqueue(reloadCurrentFolderAction);
		} else if (action instanceof OpenFileInFolderAction) {
			final OpenFileInFolderAction openInFolder = (OpenFileInFolderAction) action;
			final VFSPath folderPath = openInFolder.getFolderPath();
			final List<EntryUiModel> entries = openInFolder.getEntries();
			final ParentFolderEntryUiModel parentFolderEntryModel = openInFolder.getParentFolderEntryUiModel();
			setCurrentFolder(folderPath, parentFolderEntryModel, entries);
		} else if (action instanceof CreateFolderAction) {
			final CreateFolderAction createAction = (CreateFolderAction) action;
			final EntryUiModel entryModel = new EntryUiModel(createAction.getNewFolder(), true);
			entryTableModel.appendEntry(entryModel);
		} else if (action instanceof ImportAction) {
			// reload current folder after import
			final GetFolderContentAction reloadCurrentFolderAction = new GetFolderContentAction(this, currentFolder);
			workerController.enqueue(reloadCurrentFolderAction);
		} else if (action instanceof RenameEntryAction) {
			final RenameEntryAction renameAction = (RenameEntryAction) action;
			entryTableModel.setValueAt(renameAction.getEntryModel(), renameAction.getEditedRowIndex(), 0);
		} else if (action instanceof ExportAction) {
			final ExportAction exportAction = (ExportAction) action;
			JOptionPane.showMessageDialog((Component) getView(), "Successfully exported " + exportAction.getEntries() + " to "
					+ exportAction.getDestination().getAbsolutePath());
		} else if (action instanceof CopyAction) {
			final GetFolderContentAction reloadCurrentFolderAction = new GetFolderContentAction(this, currentFolder);
			workerController.enqueue(reloadCurrentFolderAction);
		} else if (action instanceof CutAction) {
			final GetFolderContentAction reloadCurrentFolderAction = new GetFolderContentAction(this, currentFolder);
			workerController.enqueue(reloadCurrentFolderAction);
		} else {
			LOGGER.debug("Action " + action.getClass().getName() + " not handled in " + this.getClass().getName());
		}

		updateGUI();
	}

	private void getFolderContentActionFinished(final GetFolderContentAction getFolderAction) {
		final ParentFolderEntryUiModel parentFolderEntryModel = getFolderAction.getParentFolderEntryModel();
		final List<EntryUiModel> entries = getFolderAction.getEntries();
		setCurrentFolder(getFolderAction.getFolderPath(), parentFolderEntryModel, entries);
	}

	private void setCurrentFolder(final VFSPath path, final ParentFolderEntryUiModel parentFolderEntryModel, final List<EntryUiModel> entries) {
		entryTableModel.setEntries(parentFolderEntryModel, entries);
		this.currentFolder = path;
		updateGUI();
	}

	public EntryTableModel getEntryTableModel() {
		return entryTableModel;
	}

	public void openEntry(final EntryUiModel entry) {
		final GetFolderContentAction action = new GetFolderContentAction(this, entry);
		workerController.enqueue(action);
	}

	public void startDelete(final List<EntryUiModel> entries) {
		final DeleteEntryAction action = new DeleteEntryAction(this, entries);
		workerController.enqueue(action);
	}

	public void startRenameEntry(final EntryUiModel currentEditedValue, final int editedRow, final String newEntryName) {
		final RenameEntryAction action = new RenameEntryAction(this, currentEditedValue, editedRow, newEntryName);
		workerController.enqueue(action);
	}

	private String getUniqueFolderName(final List<EntryUiModel> entries) {
		final String praefix = "NewFolder";
		String newFolderName = praefix;
		int count = 0;
		boolean isUnique;

		do {
			isUnique = true;
			for (final EntryUiModel entry : entries) {
				if (newFolderName.equals(entry.getDisplayName())) {
					isUnique = false;
					newFolderName = praefix + ++count;

					// break for loop
					break;
				}
			}
		} while (!isUnique);

		return newFolderName;
	}

	public void startCreateNewFolder() {
		final List<EntryUiModel> entries = entryTableModel.getEntries();
		final String newFolderName = getUniqueFolderName(entries);
		startCreateNewFolder(newFolderName);
	}

	public void startCreateNewFolder(final String name) {
		final CreateFolderAction action = new CreateFolderAction(this, currentFolder, name);
		workerController.enqueue(action);
	}

	public void startImportFromHostFs(final String sourcePath, final String targetPath) {
		final ImportAction action = new ImportAction(this, sourcePath, targetPath);
		workerController.enqueue(action);
	}

	public void startImportFromHostFs(final List<File> filesToImport) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Start Import from " + Arrays.toString(filesToImport.toArray()));
		}

		final List<Pair<String, String>> filePathes = new ArrayList<>();
		for (final File f : filesToImport) {

			final String hostFsSourcePath = f.getAbsolutePath();
			final String destinationPath = PathUtil.concatPathAndFileName(currentFolder.getAbsolutePath(), f.getName());

			filePathes.add(new Pair<String, String>(hostFsSourcePath, destinationPath));
		}

		final ImportAction action = new ImportAction(this, filePathes);
		workerController.enqueue(action);
	}

	public String getCurrentFolderAsString() {
		if (currentFolder == null) {
			return "";
		}
		return currentFolder.getAbsolutePath();
	}

	public void startExport(final JFrame desktopFrame, final List<EntryUiModel> entries) {
		final JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Choose File to export to");
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setFileSelectionMode(entries.size() > 1 || (entries.size() == 1 && entries.get(0).isDirectory()) ? JFileChooser.DIRECTORIES_ONLY
				: JFileChooser.FILES_ONLY);
		final int returnVal = fc.showDialog(desktopFrame, "Ok");
		if (returnVal == JFileChooser.APPROVE_OPTION) {

			final File selected = fc.getSelectedFile();
			final List<VFSEntry> vfsEntries = new ArrayList<VFSEntry>(entries.size());
			for (final EntryUiModel uiEntry : entries) {
				vfsEntries.add(uiEntry.getEntry());
			}

			if (selected.isDirectory()
					|| !selected.exists()
					|| JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(desktopFrame,
							"The selected file already exists, do you really want do overwrite it?")) {
				final ExportAction action = new ExportAction(this, vfsEntries, selected, desktopFrame);
				workerController.enqueue(action);

			}
		}
	}

	public void copyToClipboard(final List<EntryUiModel> entries) {
		LOGGER.debug("Adding " + entries + " to clipboard for copy");
		clipboard = new Pair<ClipboardAction, List<EntryUiModel>>(ClipboardAction.COPY, entries);
	}

	public void cutToClipboard(final List<EntryUiModel> entries) {
		LOGGER.debug("Adding " + entries + " to clipboard for cut");
		clipboard = new Pair<ClipboardAction, List<EntryUiModel>>(ClipboardAction.CUT, entries);
	}

	/**
	 * pastes clipboard to <code>toFolder</code>. toFolder is allowed to be null and thus the "currentFolder" is taken!
	 * 
	 * @param toFolder
	 */
	public void pasteFromClipboardTo(final EntryUiModel toFolder) {

		if (clipboard == null || toFolder != null && !toFolder.getEntry().isDirectory()) {
			return;
		}
		try {
			final VFSEntry destinationFolder = toFolder == null ? currentFolder.getVFSEntry() : toFolder.getEntry();
			final List<VFSEntry> vfsEntries = new ArrayList<VFSEntry>(clipboard.getSecond().size());
			for (final EntryUiModel entry : clipboard.getSecond()) {
				vfsEntries.add(entry.getEntry());
			}
			if (clipboard.getFirst() == ClipboardAction.COPY) {
				final CopyAction copy = new CopyAction(this, vfsEntries, destinationFolder);
				workerController.enqueue(copy);
			} else {
				for (final VFSEntry cutSource : vfsEntries) {
					final String cutSourcePath = cutSource.getPath().getAbsolutePath();

					if (destinationFolder.getPath().getAbsolutePath().contains(cutSourcePath)) {
						SwingUtil.showWarning(null, "No can do! Target folder is a descendant of cut source folder");
						return;
					}
				}
				final CutAction cut = new CutAction(this, vfsEntries, destinationFolder);
				workerController.enqueue(cut);

			}
			clipboard = null;
		} catch (final VFSException e) {
			LOGGER.error("Erro during clipoard action", e);
		}
	}

	public void createNewFolderFromContextMenu(final EntryUiModel entry) {
		final GetFolderContentAction action = new GetFolderContentAction(new ActionObserver() {

			@Override
			public void onActionFinished(final AbstractBadgerAction action) {
				getFolderContentActionFinished((GetFolderContentAction) action);
				startCreateNewFolder();
			}

			@Override
			public void onActionFailed(final AbstractBadgerAction action, final Exception e) {
				SwingUtil.handleException(null, e);
				updateGUI();
			}
		}, entry);

		workerController.enqueue(action);
	}

	public void importFromContextMenu(final EntryUiModel entry, final JFrame parent) {

		final GetFolderContentAction action = new GetFolderContentAction(new ActionObserver() {

			@Override
			public void onActionFinished(final AbstractBadgerAction action) {
				getFolderContentActionFinished((GetFolderContentAction) action);
				openImportDialog(parent);
			}

			@Override
			public void onActionFailed(final AbstractBadgerAction action, final Exception e) {
				SwingUtil.handleException(null, e);
				updateGUI();
			}
		}, entry);

		workerController.enqueue(action);
	}

	public EntryUiModel getParentFolderEntry() {
		try {
			return new ParentFolderEntryUiModel(currentFolder.getVFSEntry());
		} catch (final VFSException e) {
			LOGGER.error("error getting parentFolder entry", e);
		}
		return null;
	}

	public DiskWorkerController getWorkerController() {
		return workerController;
	}

}
