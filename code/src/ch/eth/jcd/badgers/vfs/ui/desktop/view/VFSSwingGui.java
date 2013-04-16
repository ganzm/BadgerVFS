package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.Initialisation;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.BadgerViewBase;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.ParentFolderEntryUiModel;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

@SuppressWarnings("serial")
public class VFSSwingGui extends JFrame implements BadgerViewBase {

	private static final long serialVersionUID = -8776317677851635247L;

	private static final Logger LOGGER = Logger.getLogger(VFSSwingGui.class);

	private final EntryCellEditor entryCellEditor;

	private final JPanel contentPane;
	private final JTextField textFieldFind;

	private final DesktopController desktopController = new DesktopController(this);
	private final JMenu mnActions;
	private final JMenuItem mntmNew;
	private final JMenuItem mntmOpen;
	private final JMenuItem mntmClose;
	private final JMenuItem mntmPaste;
	private final JTextField textFieldCurrentPath;
	private final JTable tableFolderEntries;
	public static final String SEARCH_PANEL_NAME = "searchpanel";
	public static final String BROWSE_PANEL_NAME = "browsepanel";
	private final SearchPanel panelSearch;
	private final JButton btnSearch;
	/**
	 * State Variable determines whether search or browse gui is shown
	 */
	private boolean searching = false;

	private final JMenuItem mntmQueryDiskspace;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		Initialisation.initApplication(args);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					final VFSSwingGui frame = new VFSSwingGui();
					frame.update();
					frame.setVisible(true);
				} catch (final Exception e) {
					LOGGER.error("", e);
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public VFSSwingGui() {
		setTitle("BadgerFS Client");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent ev) {
				beforeWindowClosing();
			}
		});

		setBounds(100, 100, 900, 631);

		final JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		final JMenu mnDisk = new JMenu("Disk");
		mnDisk.setMnemonic('D');
		menuBar.add(mnDisk);

		mntmNew = new JMenuItem(new AbstractAction("New") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				desktopController.openCreateNewDiskDialog(getDesktopFrame());
			}
		});

		mnDisk.add(mntmNew);

		mntmOpen = new JMenuItem(new AbstractAction("Open") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					desktopController.openFileChooserForDiskOpen(getDesktopFrame());
				} catch (final Exception ex) {
					SwingUtil.handleException(getDesktopFrame(), ex);
				}
			}
		});

		mnDisk.add(mntmOpen);

		mntmClose = new JMenuItem(new AbstractAction("Close") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					desktopController.closeDisk(getDesktopFrame());
					// if we open/create another disk the Browser Panel is shown
					showCardLayoutPanel(BROWSE_PANEL_NAME);
				} catch (final Exception ex) {
					SwingUtil.handleException(getDesktopFrame(), ex);
				}
			}
		});

		mnDisk.add(mntmClose);
		mnDisk.addSeparator();

		mntmQueryDiskspace = new JMenuItem(new AbstractAction("Query Diskspace") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				desktopController.openDiskSpaceDialog(getDesktopFrame());
			}
		});
		mnDisk.add(mntmQueryDiskspace);

		mnDisk.addSeparator();

		final JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
		mnDisk.add(mntmExit);

		mnActions = new JMenu("Actions");
		mnActions.setMnemonic('A');
		menuBar.add(mnActions);

		// actions on the current directory!
		// create new folder in the current directory!

		mnActions.add(new JMenuItem(new AbstractAction("New Folder") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					desktopController.startCreateNewFolder();
				} catch (final Exception ex) {
					SwingUtil.handleException(getDesktopFrame(), ex);
				}
			}
		}));

		mntmPaste = getPasteMenuItem(null);

		mnActions.add(mntmPaste);

		// open import dialog, using current folder
		mnActions.add(new JMenuItem(new AbstractAction("Import") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				desktopController.openImportDialog(getDesktopFrame());
			}
		}));

		mnActions.addSeparator();

		// TODO: allow multiple selection and do all the stuff on the selected entries (except renaming?)
		// actions on the currently selected entry

		mnActions.add(getRenameMenuItem());
		mnActions.add(getDeleteMenuItem());
		mnActions.add(getExportMenuItem());
		mnActions.add(getCopyMenuItem());
		mnActions.add(getCutMenuItem());

		final JMenu mnHelp = new JMenu("Help");
		mnHelp.setMnemonic('H');
		menuBar.add(mnHelp);

		final JMenuItem mntmInfo = new JMenuItem(new AbstractAction("Info") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					desktopController.openInfoDialog(getDesktopFrame());
				} catch (final Exception ex) {
					SwingUtil.handleException(getDesktopFrame(), ex);
				}
			}
		});
		mnHelp.add(mntmInfo);

		textFieldFind = new JTextField();
		textFieldFind.setText("Find");
		menuBar.add(textFieldFind);
		textFieldFind.setColumns(10);

		btnSearch = new JButton(new AbstractAction("Search") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				startSearch();
			}
		});

		menuBar.add(btnSearch);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		contentPane.setLayout(new CardLayout());

		final JPanel panelBrowsing = new JPanel();
		contentPane.add(panelBrowsing, BROWSE_PANEL_NAME);
		panelBrowsing.setLayout(new BorderLayout(0, 0));

		// ADDING "Enter" handler on table item
		final KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

		final JPanel panelPathLocator = new JPanel();
		panelBrowsing.add(panelPathLocator, BorderLayout.NORTH);
		final GridBagLayout gbl_panelPathLocator = new GridBagLayout();
		gbl_panelPathLocator.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelPathLocator.rowHeights = new int[] { 0, 0 };
		gbl_panelPathLocator.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelPathLocator.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelPathLocator.setLayout(gbl_panelPathLocator);

		final JLabel lblPath = new JLabel("Path");
		final GridBagConstraints gbc_lblPath = new GridBagConstraints();
		gbc_lblPath.insets = new Insets(0, 0, 0, 5);
		gbc_lblPath.anchor = GridBagConstraints.EAST;
		gbc_lblPath.gridx = 0;
		gbc_lblPath.gridy = 0;
		panelPathLocator.add(lblPath, gbc_lblPath);

		textFieldCurrentPath = new JTextField();
		textFieldCurrentPath.setEditable(false);
		final GridBagConstraints gbc_textFieldCurrentPath = new GridBagConstraints();
		gbc_textFieldCurrentPath.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldCurrentPath.gridx = 1;
		gbc_textFieldCurrentPath.gridy = 0;
		panelPathLocator.add(textFieldCurrentPath, gbc_textFieldCurrentPath);
		textFieldCurrentPath.setColumns(10);

		final JPanel panelBrowseMiddle = new JPanel();
		panelBrowsing.add(panelBrowseMiddle, BorderLayout.CENTER);
		panelBrowseMiddle.setLayout(new BorderLayout(0, 0));

		final JScrollPane scrollPaneBrowseTable = new JScrollPane();
		scrollPaneBrowseTable.addMouseListener(new MouseAdapter() {

			@Override
			// rightclick windows/linux
			public void mousePressed(final MouseEvent e) {
				doContextMenuOnTable(e);
			}

			@Override
			// rightclick mac
			public void mouseReleased(final MouseEvent e) {
				doContextMenuOnTable(e);
			}
		});
		panelBrowseMiddle.add(scrollPaneBrowseTable, BorderLayout.CENTER);

		tableFolderEntries = new JTable();
		tableFolderEntries.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPaneBrowseTable.setViewportView(tableFolderEntries);
		tableFolderEntries.setShowGrid(false);
		tableFolderEntries.setDefaultRenderer(EntryUiModel.class, new EntryListCellRenderer());
		tableFolderEntries.setRowHeight(40);
		tableFolderEntries.setModel(desktopController.getEntryTableModel());

		final TableColumn columnModel = tableFolderEntries.getColumnModel().getColumn(0);
		entryCellEditor = new EntryCellEditor(tableFolderEntries, desktopController);
		columnModel.setCellEditor(entryCellEditor);

		// mouse listeners ond jtable
		tableFolderEntries.addMouseListener(new MouseAdapter() {

			@Override
			// doubleclick
			public void mouseClicked(final MouseEvent event) {
				if (event.getClickCount() >= 2) {
					final int rowIndex = tableFolderEntries.rowAtPoint(event.getPoint());
					final EntryUiModel entry = (EntryUiModel) tableFolderEntries.getModel().getValueAt(rowIndex, 0);
					LOGGER.debug("Doubleclicked " + entry);
					if (entry != null && entry.isDirectory()) {
						desktopController.openEntry(entry);
					}
				}
			}

			@Override
			// rightclick windows/linux
			public void mousePressed(final MouseEvent e) {
				doContextMenuOnTable(e);
			}

			@Override
			// rightclick mac
			public void mouseReleased(final MouseEvent e) {
				doContextMenuOnTable(e);
			}

		});

		// adjusting menuItems when selection in table changes
		tableFolderEntries.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				final EntryUiModel entry = (EntryUiModel) tableFolderEntries.getValueAt(tableFolderEntries.getSelectedRow(), 0);
				adjustActionMenus(entry);
			}

		});
		tableFolderEntries.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, "enter");
		tableFolderEntries.getActionMap().put("enter", new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (tableFolderEntries.isEditing()) {
					// we are currently renaming an entry
					// hitting Enter should stop that
					entryCellEditor.stopCellEditing();
					return;
				}
				final EntryUiModel entry = (EntryUiModel) tableFolderEntries.getValueAt(tableFolderEntries.getSelectedRow(), 0);
				if (entry.isDirectory()) {
					desktopController.openEntry(entry);
				} else {
					desktopController.startExport(getDesktopFrame(), Arrays.asList(new EntryUiModel[] { entry }));
				}
			}
		});

		removeKeysFromJTableInputMap(tableFolderEntries);

		panelSearch = new SearchPanel(this);
		contentPane.add(panelSearch, SEARCH_PANEL_NAME);
	}

	private JMenuItem getPasteMenuItem(final EntryUiModel entry) {
		// paste into the current folder
		final JMenuItem mntmPaste = new JMenuItem(new AbstractAction("Paste") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				// null means use current folder
				desktopController.pasteFromClipboardTo(entry);
			}
		});
		mntmPaste.setAccelerator(KeyStroke.getKeyStroke('V', InputEvent.CTRL_DOWN_MASK));
		return mntmPaste;
	}

	private JMenuItem getCutMenuItem() {
		// cuts the currently selected entries to the clipboard
		final JMenuItem retVal = new JMenuItem(new AbstractAction("Cut") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				desktopController.cutToClipboard(getSelectedEntries());
			}
		});
		retVal.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_DOWN_MASK));
		return retVal;
	}

	private JMenuItem getCopyMenuItem() {
		// copies the currently selected entry to the clipboard
		final JMenuItem retVal = new JMenuItem(new AbstractAction("Copy") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				desktopController.copyToClipboard(getSelectedEntries());
			}
		});
		retVal.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK));
		return retVal;
	}

	private JMenuItem getExportMenuItem() {
		// exports the currently selected entry
		final JMenuItem exportItem = new JMenuItem(new AbstractAction("Export") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				startExport();
			}

			private void startExport() {
				final List<EntryUiModel> selectedItems = getSelectedEntries();
				if (selectedItems.isEmpty()) {
					selectedItems.add(desktopController.getParentFolderEntry());
				}

				desktopController.startExport(getDesktopFrame(), selectedItems);
			}
		});
		exportItem.setAccelerator(KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK));
		return exportItem;
	}

	private JMenuItem getDeleteMenuItem() {
		// deletes the currently selected entry
		final JMenuItem retVal = new JMenuItem(new AbstractAction("Delete") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				startDelete();
			}
		});
		retVal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		return retVal;
	}

	private JMenuItem getRenameMenuItem() {
		// renames the currently selected entry
		final JMenuItem retVal = new JMenuItem(new AbstractAction("Rename") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				startRename();
			}
		});
		retVal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		return retVal;
	}

	private void adjustActionMenus(final EntryUiModel entry) {
		mntmPaste.setEnabled(entry == null || entry.isDirectory());
	}

	protected void startSearch() {
		panelSearch.resetSearch();
		panelSearch.setSearchTextAndContext(textFieldFind.getText(), desktopController.getCurrentFolderAsString());

		showCardLayoutPanel(SEARCH_PANEL_NAME);
	}

	public void showCardLayoutPanel(final String panelName) {
		final CardLayout cl = (CardLayout) (contentPane.getLayout());
		cl.show(contentPane, panelName);

		searching = SEARCH_PANEL_NAME.equals(panelName);

		if (searching) {
			panelSearch.update();
		}

		update();
	}

	/**
	 * copied from http://stackoverflow.com/questions/2019371/swing-setting-a-function-key-f2-as-an-accelerator
	 * 
	 * @param tableFolderEntries2
	 */
	private void removeKeysFromJTableInputMap(final JTable tableFolderEntries2) {
		final KeyStroke f2KeyToRemove = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
		final KeyStroke deleteKeyToRemove = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		final KeyStroke ctrlC = KeyStroke.getKeyStroke('C', KeyEvent.CTRL_DOWN_MASK);
		final KeyStroke ctrlX = KeyStroke.getKeyStroke('X', KeyEvent.CTRL_DOWN_MASK);
		final KeyStroke ctrlV = KeyStroke.getKeyStroke('V', KeyEvent.CTRL_DOWN_MASK);

		InputMap imap = tableFolderEntries.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		while (imap != null) {
			imap.remove(f2KeyToRemove);
			imap.remove(deleteKeyToRemove);
			imap.remove(ctrlC);
			imap.remove(ctrlX);
			imap.remove(ctrlV);
			imap = imap.getParent();
		}
	}

	private void startRename() {
		try {
			final int currentRow = tableFolderEntries.getSelectedRow();
			if (currentRow < 0) {
				JOptionPane.showMessageDialog(this, "Selecte File or Folder", "Badger Message", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			final EntryUiModel entry = (EntryUiModel) tableFolderEntries.getModel().getValueAt(currentRow, 0);
			LOGGER.debug("Start renaming " + entry);

			entryCellEditor.setAllowEditing(true);
			tableFolderEntries.editCellAt(currentRow, 0);
		} catch (final Exception ex) {
			SwingUtil.handleException(getDesktopFrame(), ex);
		}
	}

	private void startDelete() {
		try {
			final List<EntryUiModel> selectedEntries = getSelectedEntries();
			if (selectedEntries.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Select File or Folder", "Badger Message", JOptionPane.INFORMATION_MESSAGE);
				return;

			}
			desktopController.startDelete(selectedEntries);
		} catch (final Exception ex) {
			SwingUtil.handleException(getDesktopFrame(), ex);
		}
	}

	private List<EntryUiModel> getSelectedEntries() {
		final int[] selectedRowIdexes = tableFolderEntries.getSelectedRows();
		final List<EntryUiModel> selectedEntries = new ArrayList<EntryUiModel>(selectedRowIdexes.length);
		for (final int i : selectedRowIdexes) {
			final EntryUiModel uiEntry = (EntryUiModel) tableFolderEntries.getValueAt(i, 0);
			if (!(uiEntry instanceof ParentFolderEntryUiModel)) {
				selectedEntries.add(uiEntry);
			}
		}
		return selectedEntries;
	}

	/**
	 * helper method for anonymous inner classes (ActionListenerImpl.) to get "this"
	 * 
	 * @return
	 */
	private JFrame getDesktopFrame() {
		return this;
	}

	private void doContextMenuOnTable(final MouseEvent e) {
		if (!e.isPopupTrigger()) {
			return;
		}
		LOGGER.trace(e);
		final int rowIndex = tableFolderEntries.rowAtPoint(e.getPoint());
		// rowIndex = -1 means that the user clicked in grey area where no items are --> we use the parentfolder in this case otherwise we get the item at the
		// selected row
		final EntryUiModel entry = rowIndex == -1 ? desktopController.getParentFolderEntry() : (EntryUiModel) tableFolderEntries.getModel().getValueAt(
				rowIndex, 0);
		// tableFolderEntries.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);

		LOGGER.debug("Opening popup on entry: " + entry.getFullPath());
		final JPopupMenu menu = new JPopupMenu();
		if (entry.isDirectory()) {

			// new folder
			menu.add(new JMenuItem(new AbstractAction("New Folder") {
				@Override
				public void actionPerformed(final ActionEvent e) {
					desktopController.createNewFolderFromContextMenu(entry);
				}
			}));

			// paste
			menu.add(getPasteMenuItem(entry));

			// import
			menu.add(new JMenuItem(new AbstractAction("Import") {
				@Override
				public void actionPerformed(final ActionEvent e) {
					desktopController.importFromContextMenu(entry, getDesktopFrame());
				}
			}));
		}

		menu.addSeparator();

		if (!(entry instanceof ParentFolderEntryUiModel)) {
			menu.add(getRenameMenuItem());
			menu.add(getDeleteMenuItem());
		}
		menu.add(getExportMenuItem());
		menu.add(getCopyMenuItem());
		menu.add(getCutMenuItem());

		menu.show(((Container) e.getSource()).getComponentAt(e.getPoint()), e.getX(), e.getY());

	}

	private void beforeWindowClosing() {
		if (!desktopController.isInManagementMode()) {
			final Object[] options = new Object[] { "Yes", "No" };
			final int retVal = JOptionPane.showOptionDialog(getDesktopFrame(), "Disk is still opened. Do you want me to close it before we exit?",
					"Close Disk?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (retVal == JOptionPane.YES_OPTION) {
				try {
					desktopController.closeDisk(getDesktopFrame());
				} catch (final VFSException e) {
					SwingUtil.handleException(getDesktopFrame(), e);
				}
			} else {
				return;
			}
		}

		// close the main window
		dispose();
	}

	@Override
	public void update() {

		final boolean diskMode = !desktopController.isInManagementMode();

		mnActions.setEnabled(diskMode && !searching);
		mntmClose.setEnabled(diskMode);
		mntmNew.setEnabled(!diskMode);
		mntmOpen.setEnabled(!diskMode);
		mntmQueryDiskspace.setEnabled(diskMode);

		contentPane.setVisible(diskMode);
		contentPane.setEnabled(diskMode);

		btnSearch.setEnabled(diskMode && !searching);
		textFieldFind.setEnabled(!searching);
		textFieldCurrentPath.setText(desktopController.getCurrentFolderAsString());
	}

}
