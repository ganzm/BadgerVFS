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
import ch.eth.jcd.badgers.vfs.remote.model.DiskRemoteResult;
import ch.eth.jcd.badgers.vfs.sync.client.ConnectionStateListener;
import ch.eth.jcd.badgers.vfs.sync.client.ConnectionStatus;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.sync.client.ServerVersionChangedListener;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.DefaultObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.CopyAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.CreateFolderAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.CutAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.DeleteEntryAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.DownloadRemoteChangesAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.ExportAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.GetFolderContentAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.GetServerVersionAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.ImportAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.LinkCurrentDiskAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.RenameEntryAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.UploadLocalChangesAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.UseCurrentLinkedDiskAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.BadgerFileExtensionFilter;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryTableModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.ParentFolderEntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.RemoteSynchronisationWizardContext;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.RemoteSynchronisationWizardContext.LoginActionEnum;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.DiskSpaceDialog;
import ch.eth.jcd.badgers.vfs.ui.desktop.view.GetRemoteLinkedDiskDialog;
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

public class DesktopController extends BadgerController implements ConnectionStateListener, ServerVersionChangedListener {
	public enum ClipboardAction {
		COPY, CUT
	}

	private final ActionObserver getFolderContentActionHandler = new DefaultObserver(this) {
		@Override
		public void onActionFinished(final AbstractBadgerAction action) {
			getFolderContentActionFinished((GetFolderContentAction) action);
			updateGUI();
		}
	};

	private final ActionObserver importActionHandler = new DefaultObserver(this) {
		@Override
		public void onActionFinished(final AbstractBadgerAction action) {
			// reload current folder after import
			final GetFolderContentAction reloadCurrentFolderAction = new GetFolderContentAction(getFolderContentActionHandler, currentFolder);
			workerController.enqueue(reloadCurrentFolderAction);
			updateGUI();
		}
	};
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
	public void openCreateNewDiskDialog() {
		final NewDiskCreationDialog dialog = new NewDiskCreationDialog(this);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openCreateNewRemoteDiskDialog(final RemoteSynchronisationWizardContext wizard) {
		final NewRemoteDiskCreationDialog dialog = new NewRemoteDiskCreationDialog(this, wizard);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openImportDialog() {
		final ImportDialog dialog = new ImportDialog(this, currentFolder.getAbsolutePath());
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openDiskSpaceDialog() {
		final DiskSpaceDialog dialog = new DiskSpaceDialog(this);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openInfoDialog() {
		final InfoDialog dialog = new InfoDialog(this);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openLinkDiskDialog() {
		final ServerUrlDialog dialog = new ServerUrlDialog(this, new RemoteSynchronisationWizardContext(LoginActionEnum.SYNC));
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openConnectRemoteDialog() {
		final ServerUrlDialog dialog = new ServerUrlDialog(this, new RemoteSynchronisationWizardContext(LoginActionEnum.LOGINREMOTE));
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openLoginDialog(final RemoteSynchronisationWizardContext wizardContext) {
		final LoginDialog dialog = new LoginDialog(this, wizardContext);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openGetRemoteLinkedDiskDialog(final RemoteSynchronisationWizardContext wizardContext) {
		final GetRemoteLinkedDiskDialog dialog = new GetRemoteLinkedDiskDialog(this, wizardContext);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public void openLinkedDisk() throws VFSException {
		final ActionObserver handler = new DefaultObserver(this) {

			@Override
			public void onActionFinished(final AbstractBadgerAction action) {
				startSynchronization();
				updateGUI();
			}
		};
		final UseCurrentLinkedDiskAction action = new UseCurrentLinkedDiskAction(handler, remoteManager);
		workerController.enqueue(action);
	}

	public void startSyncToServer(final RemoteSynchronisationWizardContext wizardContext) {
		final ActionObserver handler = new DefaultObserver(this) {

			@Override
			public void onActionFinished(final AbstractBadgerAction action) {
				remoteManager = wizardContext.getRemoteManager();
				remoteManager.setServerVersionChangedListener(getThis());
				updateGUI();
				startSynchronization();
			}
		};
		final LinkCurrentDiskAction action = new LinkCurrentDiskAction(handler, wizardContext.getRemoteManager());
		workerController.enqueue(action);
	}

	private DesktopController getThis() {
		return this;
	}

	public void startSynchronization() {
		ActionObserver handler = new DefaultObserver(this) {

			@Override
			public void onActionFinished(AbstractBadgerAction action) {
				// reload current folder
				final GetFolderContentAction reloadCurrentFolderAction = new GetFolderContentAction(getFolderContentActionHandler, currentFolder);
				workerController.enqueue(reloadCurrentFolderAction);
				updateGUI();
			}
		};

		DownloadRemoteChangesAction downloadAction = new DownloadRemoteChangesAction(null, remoteManager);
		workerController.enqueue(downloadAction);

		UploadLocalChangesAction uploadAction = new UploadLocalChangesAction(handler, remoteManager);
		workerController.enqueue(uploadAction);

	}

	public void openRemoteDiskDialog(final RemoteSynchronisationWizardContext wizard) {
		final RemoteDiskDialog dialog = new RemoteDiskDialog(this, wizard);
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
			openDiskFromFile(selectedFile);
		}
	}

	public void openDiskFromFile(File file) throws VFSException {
		initDisk(file.getAbsolutePath());
		if (remoteManager != null) {
			final RemoteSynchronisationWizardContext wizardContext = new RemoteSynchronisationWizardContext(LoginActionEnum.CONNECT);
			wizardContext.setRemoteManager(remoteManager);
			openLoginDialog(wizardContext);
		}
	}

	/**
	 * Initialize the disk for opening.
	 * 
	 * @param path
	 */
	private void initDisk(final String path) throws VFSException {
		if (!isInManagementMode()) {
			throw new VFSException("Cannot open Disk on " + path + " - close current disk first");
		}

		LOGGER.debug("Opening Disk from Path " + path);

		final DiskConfiguration config = new DiskConfiguration();
		config.setHostFilePath(path);

		final VFSDiskManagerFactory factory = VFSDiskManagerFactory.getInstance();
		final VFSDiskManager diskManager = factory.openDiskManager(config);

		remoteManager = initRemoteManager(config);

		workerController = new DiskWorkerController(diskManager);
		workerController.startWorkerController();

		loadRootFolder();

		updateGUI();
	}

	private RemoteManager initRemoteManager(final DiskConfiguration config) {
		final String hostLink = config.getLinkedHostName();
		if (hostLink == null || hostLink.isEmpty()) {
			LOGGER.debug("Disk not linked");
			return null;
		}

		final RemoteManager mgr = new RemoteManager(hostLink);
		mgr.addConnectionStateListener(this);
		mgr.setServerVersionChangedListener(this);
		mgr.start();
		return mgr;
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

		final GetFolderContentAction getFolderContentAction = new GetFolderContentAction(getFolderContentActionHandler);
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
	public void closeDisk() throws VFSException {
		if (isInManagementMode()) {
			throw new VFSException("Cannot close new Disk - no current disk opened");
		}

		workerController.dispose();
		workerController = null;
		if (remoteManager != null) {
			remoteManager.removeConnectionStateListener(this);
			remoteManager.startCloseDisk();
		}
		updateGUI();
	}

	/**
	 * You have clicked on the Disk / Close menu item
	 * 
	 * @param desktopFrame
	 * @throws VFSException
	 */
	public void disposeDisk() throws VFSException {
		if (isInManagementMode()) {
			throw new VFSException("Cannot close new Disk - no current disk opened");
		}
		VFSDiskManager manager = workerController.getDiskManager();
		workerController.dispose();
		workerController = null;
		if (remoteManager != null) {
			remoteManager.removeConnectionStateListener(this);
			remoteManager.startCloseDisk();
			remoteManager.logout();
			remoteManager.dispose();
		}
		manager.dispose();
		updateGUI();
	}

	private void getFolderContentActionFinished(final GetFolderContentAction getFolderAction) {
		final ParentFolderEntryUiModel parentFolderEntryModel = getFolderAction.getParentFolderEntryModel();
		final List<EntryUiModel> entries = getFolderAction.getEntries();
		setCurrentFolder(getFolderAction.getFolderPath(), parentFolderEntryModel, entries);
	}

	public void setCurrentFolder(final VFSPath path, final ParentFolderEntryUiModel parentFolderEntryModel, final List<EntryUiModel> entries) {
		entryTableModel.setEntries(parentFolderEntryModel, entries);
		this.currentFolder = path;
		updateGUI();
	}

	public EntryTableModel getEntryTableModel() {
		return entryTableModel;
	}

	public void openEntry(final EntryUiModel entry) {
		final GetFolderContentAction action = new GetFolderContentAction(getFolderContentActionHandler, entry);
		workerController.enqueue(action);
	}

	public void startDelete(final List<EntryUiModel> entries) {
		final ActionObserver handler = new DefaultObserver(this) {
			@Override
			public void onActionFinished(final AbstractBadgerAction action) {
				final GetFolderContentAction reloadCurrentFolderAction = new GetFolderContentAction(getFolderContentActionHandler, currentFolder);
				workerController.enqueue(reloadCurrentFolderAction);
				updateGUI();
			}
		};
		final DeleteEntryAction action = new DeleteEntryAction(handler, entries);
		workerController.enqueue(action);
	}

	public void startRenameEntry(final EntryUiModel currentEditedValue, final int editedRow, final String newEntryName) {
		final ActionObserver handler = new DefaultObserver(this) {
			@Override
			public void onActionFinished(final AbstractBadgerAction action) {
				final RenameEntryAction renameAction = (RenameEntryAction) action;
				entryTableModel.setValueAt(renameAction.getEntryModel(), renameAction.getEditedRowIndex(), 0);
			}
		};
		final RenameEntryAction action = new RenameEntryAction(handler, currentEditedValue, editedRow, newEntryName);
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
		final ActionObserver handler = new DefaultObserver(this) {
			@Override
			public void onActionFinished(final AbstractBadgerAction action) {
				final CreateFolderAction createAction = (CreateFolderAction) action;
				final EntryUiModel entryModel = new EntryUiModel(createAction.getNewFolder(), true);
				entryTableModel.appendEntry(entryModel);
				updateGUI();
			}
		};
		final CreateFolderAction action = new CreateFolderAction(handler, currentFolder, name);
		workerController.enqueue(action);
	}

	public void startImportFromHostFs(final String sourcePath, final String targetPath) {

		final ImportAction action = new ImportAction(importActionHandler, sourcePath, targetPath);
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

		final ImportAction action = new ImportAction(importActionHandler, filePathes);
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
				final DefaultObserver handler = new DefaultObserver(this) {
					@Override
					public void onActionFinished(final AbstractBadgerAction action) {
						final ExportAction exportAction = (ExportAction) action;
						JOptionPane.showMessageDialog((Component) getView(), "Successfully exported " + exportAction.getEntries() + " to "
								+ exportAction.getDestination().getAbsolutePath());
						updateGUI();
					}
				};
				final ExportAction action = new ExportAction(handler, vfsEntries, selected, desktopFrame);
				workerController.enqueue(action);

			}
		}
	}

	public void startWorkOffline() throws VFSException {
		LOGGER.debug("Start Work Offline.");
		disconnect();
	}

	public void startConnect() throws VFSException {
		remoteManager = initRemoteManager(workerController.getDiskManager().getDiskConfiguration());
		final RemoteSynchronisationWizardContext wizardContext = new RemoteSynchronisationWizardContext(LoginActionEnum.CONNECT);
		wizardContext.setRemoteManager(remoteManager);
		openLoginDialog(wizardContext);
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
				handleCopy(destinationFolder, vfsEntries);
			} else {
				handleCut(destinationFolder, vfsEntries);
			}
			clipboard = null;
		} catch (final VFSException e) {
			LOGGER.error("Erro during clipoard action", e);
		}
	}

	private void handleCut(final VFSEntry destinationFolder, final List<VFSEntry> vfsEntries) {
		for (final VFSEntry cutSource : vfsEntries) {
			final String cutSourcePath = cutSource.getPath().getAbsolutePath();

			if (destinationFolder.getPath().getAbsolutePath().contains(cutSourcePath)) {
				SwingUtil.showWarning(null, "No can do! Target folder is a descendant of cut source folder");
				return;
			}
		}
		final ActionObserver handler = new DefaultObserver(this) {
			@Override
			public void onActionFinished(final AbstractBadgerAction action) {
				final GetFolderContentAction reloadCurrentFolderAction = new GetFolderContentAction(getFolderContentActionHandler, currentFolder);
				workerController.enqueue(reloadCurrentFolderAction);
			}
		};
		final CutAction cut = new CutAction(handler, vfsEntries, destinationFolder);
		workerController.enqueue(cut);
	}

	private void handleCopy(final VFSEntry destinationFolder, final List<VFSEntry> vfsEntries) {
		final ActionObserver handler = new DefaultObserver(this) {
			@Override
			public void onActionFinished(final AbstractBadgerAction action) {
				final GetFolderContentAction reloadCurrentFolderAction = new GetFolderContentAction(getFolderContentActionHandler, currentFolder);
				workerController.enqueue(reloadCurrentFolderAction);
				updateGUI();
			}
		};
		final CopyAction copy = new CopyAction(handler, vfsEntries, destinationFolder);
		workerController.enqueue(copy);
	}

	public void createNewFolderFromContextMenu(final EntryUiModel entry) {
		final GetFolderContentAction action = new GetFolderContentAction(new DefaultObserver(this) {

			@Override
			public void onActionFinished(final AbstractBadgerAction action) {
				getFolderContentActionFinished((GetFolderContentAction) action);
				startCreateNewFolder();
				updateGUI();
			}
		}, entry);

		workerController.enqueue(action);
	}

	public void importFromContextMenu(final EntryUiModel entry, final JFrame parent) {

		final GetFolderContentAction action = new GetFolderContentAction(new DefaultObserver(this) {
			@Override
			public void onActionFinished(final AbstractBadgerAction action) {
				getFolderContentActionFinished((GetFolderContentAction) action);
				openImportDialog();
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

	public void disconnect() throws VFSException {
		if (isInManagementMode()) {
			throw new VFSException("Cannot close new Disk - no current disk opened");
		}

		if (remoteManager != null) {
			remoteManager.removeConnectionStateListener(this);
			remoteManager.startCloseDisk();
			remoteManager.logout();
			remoteManager.dispose();
		}
		updateGUI();

	}

	public void closeAndLogout() throws VFSException {
		closeDisk();
		if (remoteManager != null) {
			remoteManager.logout();
			remoteManager.dispose();
		}
	}

	public String getStatusText() {
		if (remoteManager != null) {
			return remoteManager.getConnectionStatus() + " " + remoteManager.getHostLink();
		}

		return null;
	}

	public boolean isDiskConnectedWithServer() {
		if (remoteManager != null) {
			return remoteManager.getConnectionStatus() == ConnectionStatus.DISK_MODE;
		}
		return false;
	}

	public RemoteManager getRemoteManager() {
		return remoteManager;
	}

	@Override
	public void connectionStateChanged(final ConnectionStatus status) {
		updateGUI();
	}

	@Override
	public void serverVersionChanged(DiskRemoteResult remoteResult) {
		try {
			GetServerVersionAction diskAction = new GetServerVersionAction();
			workerController.enqueueBlocking(diskAction, true);

			if (diskAction.getServerVersion() < remoteResult.getServerVersion()) {
				LOGGER.info("ServerVersion Changed to " + remoteResult.getServerVersion() + " Local Version is " + diskAction.getServerVersion());
				startSynchronization();
			}
		} catch (InterruptedException | VFSException e) {
			LOGGER.error("", e);
		}
	}

	public String getDiskPath() {
		try {
			return workerController.getDiskManager().getDiskConfiguration().getHostFilePath();
		} catch (NullPointerException | VFSException ex) {
			return "";
		}
	}

	public boolean isDiskLinked() throws VFSException {
		if (workerController != null) {
			return workerController.getDiskManager().getDiskConfiguration().isHostNameLinked();
		}
		return true;
	}

}
