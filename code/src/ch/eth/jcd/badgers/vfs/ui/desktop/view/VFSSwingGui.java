package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.Initialisation;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.BadgerViewBase;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiTreeNode;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

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
	private final JMenuItem mntmExport;
	private final JMenuItem mntmPaste;
	private final JTextField textFieldCurrentPath;
	private final JTable tableFolderEntries;
	private final JTree folderTree;
	public static final String SEARCH_PANEL_NAME = "searchpanel";
	public static final String BROWSE_PANEL_NAME = "browsepanel";
	private final SearchPanel panelSearch;
	private final JPanel panelBrowsing;
	private final JButton btnSearch;
	/**
	 * State Variable determines whether search or browse gui is shown
	 */
	private boolean searching = false;

	private JMenuItem mntmQueryDiskspace;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		Initialisation.initApplication(args);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					VFSSwingGui frame = new VFSSwingGui();
					frame.update();
					frame.setVisible(true);
				} catch (Exception e) {
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
			public void windowClosing(WindowEvent ev) {
				beforeWindowClosing();
			}
		});

		setBounds(100, 100, 900, 631);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnDisk = new JMenu("Disk");
		mnDisk.setMnemonic('D');
		menuBar.add(mnDisk);

		mntmNew = new JMenuItem("New");
		mntmNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				desktopController.openCreateNewDiskDialog(getDesktopFrame());
			}
		});
		mnDisk.add(mntmNew);

		mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					desktopController.openFileChooserForDiskOpen(getDesktopFrame());
				} catch (Exception ex) {
					SwingUtil.handleException(getDesktopFrame(), ex);
				}
			}
		});
		mnDisk.add(mntmOpen);

		mntmClose = new JMenuItem("Close");
		mntmClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					desktopController.closeDisk(getDesktopFrame());
					// if we open/create another disk the Browser Panel is shown
					showCardLayoutPanel(BROWSE_PANEL_NAME);
				} catch (Exception ex) {
					SwingUtil.handleException(getDesktopFrame(), ex);
				}
			}
		});
		mnDisk.add(mntmClose);
		mnDisk.addSeparator();

		mntmQueryDiskspace = new JMenuItem("Query Diskspace TODO");
		mnDisk.add(mntmQueryDiskspace);

		mnDisk.addSeparator();

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
		mnDisk.add(mntmExit);

		mnActions = new JMenu("Actions");
		mnActions.setMnemonic('A');
		menuBar.add(mnActions);

		// actions on the current directory!
		// create new folder in the current directory!
		JMenuItem mntmNewFolder = new JMenuItem("New Folder");
		mntmNewFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					desktopController.startCreateNewFolder();
				} catch (Exception ex) {
					SwingUtil.handleException(getDesktopFrame(), ex);
				}
			}
		});
		mnActions.add(mntmNewFolder);

		// paste into the current folder
		mntmPaste = new JMenuItem("Paste");
		mntmPaste.setAccelerator(KeyStroke.getKeyStroke('V', InputEvent.CTRL_DOWN_MASK));
		mntmPaste.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// null means use current folder
				desktopController.pasteFromClipboardTo(null);
			}
		});

		mnActions.add(mntmPaste);

		// open import dialog, using current folder
		JMenuItem mntmImport = new JMenuItem("Import");
		mntmImport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				desktopController.openImportDialog(getDesktopFrame());
			}
		});
		mnActions.add(mntmImport);

		mnActions.addSeparator();

		// TODO: allow multiple selection and do all the stuff on the selected entries (except renaming?)
		// actions on the currently selected entry
		// renames the currently selected entry
		JMenuItem mntmRename = new JMenuItem("Rename");
		mntmRename.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		mntmRename.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					startRename();
				} catch (Exception ex) {
					SwingUtil.handleException(getDesktopFrame(), ex);
				}
			}
		});
		mnActions.add(mntmRename);

		// deletes the currently selected entry
		JMenuItem mntmDelete = new JMenuItem("Delete");
		mntmDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		mntmDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					startDelete();
				} catch (Exception ex) {
					SwingUtil.handleException(getDesktopFrame(), ex);
				}
			}
		});
		mnActions.add(mntmDelete);

		// exports the currently selected entry
		mntmExport = new JMenuItem("Export");
		mntmExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				EntryUiModel entry = (EntryUiModel) tableFolderEntries.getValueAt(tableFolderEntries.getSelectedRow(), 0);
				if (entry != null && entry.isDirectory()) {
					desktopController.startExport(getDesktopFrame(), entry);
				}
			}
		});
		mnActions.add(mntmExport);

		// copies the currently selected entry to the clipboard
		JMenuItem mntmCopy = new JMenuItem("Copy");
		mntmCopy.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK));
		mntmCopy.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				EntryUiModel entry = (EntryUiModel) tableFolderEntries.getValueAt(tableFolderEntries.getSelectedRow(), 0);
				desktopController.copyToClipboard(entry);

			}
		});
		mnActions.add(mntmCopy);

		// cuts the currently selected entries to the clipboard
		JMenuItem mntmCut = new JMenuItem("Cut");
		mntmCut.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_DOWN_MASK));
		mnActions.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				EntryUiModel entry = (EntryUiModel) tableFolderEntries.getValueAt(tableFolderEntries.getSelectedRow(), 0);
				desktopController.cutToClipboard(entry);
			}
		});
		mnActions.add(mntmCut);

		JMenu mnHelp = new JMenu("Help");
		mnHelp.setMnemonic('H');
		menuBar.add(mnHelp);

		JMenuItem mntmInfo = new JMenuItem("Info");
		mnHelp.add(mntmInfo);

		textFieldFind = new JTextField();
		textFieldFind.setText("Find");
		menuBar.add(textFieldFind);
		textFieldFind.setColumns(10);

		btnSearch = new JButton("Search");
		btnSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startSearch();
			}
		});
		menuBar.add(btnSearch);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("/", true);
		contentPane.setLayout(new CardLayout());

		panelBrowsing = new JPanel();
		contentPane.add(panelBrowsing, BROWSE_PANEL_NAME);
		panelBrowsing.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		panelBrowsing.add(splitPane, BorderLayout.CENTER);

		JPanel panelBrowseMiddle = new JPanel();
		splitPane.setRightComponent(panelBrowseMiddle);
		panelBrowseMiddle.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panelBrowseMiddle.add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel lblPath = new JLabel("Path");
		GridBagConstraints gbc_lblPath = new GridBagConstraints();
		gbc_lblPath.insets = new Insets(0, 0, 0, 5);
		gbc_lblPath.anchor = GridBagConstraints.EAST;
		gbc_lblPath.gridx = 0;
		gbc_lblPath.gridy = 0;
		panel.add(lblPath, gbc_lblPath);

		textFieldCurrentPath = new JTextField();
		textFieldCurrentPath.setEditable(false);
		GridBagConstraints gbc_textFieldCurrentPath = new GridBagConstraints();
		gbc_textFieldCurrentPath.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldCurrentPath.gridx = 1;
		gbc_textFieldCurrentPath.gridy = 0;
		panel.add(textFieldCurrentPath, gbc_textFieldCurrentPath);
		textFieldCurrentPath.setColumns(10);

		JScrollPane scrollPane = new JScrollPane();
		panelBrowseMiddle.add(scrollPane, BorderLayout.CENTER);

		tableFolderEntries = new JTable();
		scrollPane.setViewportView(tableFolderEntries);
		tableFolderEntries.setShowGrid(false);
		tableFolderEntries.setDefaultRenderer(EntryUiModel.class, new EntryListCellRenderer());
		tableFolderEntries.setRowHeight(40);
		tableFolderEntries.setModel(desktopController.getEntryTableModel());

		TableColumn columnModel = tableFolderEntries.getColumnModel().getColumn(0);
		entryCellEditor = new EntryCellEditor(tableFolderEntries, desktopController);
		columnModel.setCellEditor(entryCellEditor);

		// mouse listeners ond jtable
		tableFolderEntries.addMouseListener(new MouseAdapter() {

			@Override
			// doubleclick
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() >= 2) {
					int rowIndex = tableFolderEntries.rowAtPoint(event.getPoint());
					EntryUiModel entry = (EntryUiModel) tableFolderEntries.getModel().getValueAt(rowIndex, 0);
					LOGGER.debug("Doubleclicked " + entry);
					if (entry != null && entry.isDirectory()) {
						desktopController.openEntry(entry);
					}
				}
			}

			@Override
			// rightclick windows/linux
			public void mousePressed(MouseEvent e) {
				int rowIndex = tableFolderEntries.rowAtPoint(e.getPoint());
				EntryUiModel entry = (EntryUiModel) tableFolderEntries.getModel().getValueAt(rowIndex, 0);
				tableFolderEntries.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
				if (e.isPopupTrigger()) {
					doContextMenuOnEntry(e, entry);
				}
			}

			@Override
			// rightclick mac
			public void mouseReleased(MouseEvent e) {
				int rowIndex = tableFolderEntries.rowAtPoint(e.getPoint());
				EntryUiModel entry = (EntryUiModel) tableFolderEntries.getModel().getValueAt(rowIndex, 0);
				tableFolderEntries.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
				if (e.isPopupTrigger()) {
					doContextMenuOnEntry(e, entry);
				}
			}

		});

		// adjusting menuItems when selection in table changes
		tableFolderEntries.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				EntryUiModel entry = (EntryUiModel) tableFolderEntries.getValueAt(tableFolderEntries.getSelectedRow(), 0);
				adjustActionMenus(entry);
			}

		});

		// ADDING "Enter" handler on table item
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		tableFolderEntries.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, "enter");
		tableFolderEntries.getActionMap().put("enter", new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (tableFolderEntries.isEditing()) {
					// we are currently renaming an entry
					// hitting Enter should stop that
					entryCellEditor.stopCellEditing();
					return;
				}

				EntryUiModel entry = (EntryUiModel) tableFolderEntries.getValueAt(tableFolderEntries.getSelectedRow(), 0);
				if (entry.isDirectory()) {
					desktopController.openEntry(entry);
				} else {
					desktopController.startExport(getDesktopFrame(), entry);
				}
			}
		});

		removeKeysFromJTableInputMap(tableFolderEntries);

		JPanel panelBrowseLeft = new JPanel();
		splitPane.setLeftComponent(panelBrowseLeft);
		panelBrowseLeft.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPaneFolderTree = new JScrollPane();
		panelBrowseLeft.add(scrollPaneFolderTree);
		folderTree = new JTree(desktopController.getEntryTreeModel());
		folderTree.setCellRenderer(new EntryTreeCellRenderer());
		folderTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		scrollPaneFolderTree.setViewportView(folderTree);

		folderTree.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent evt) {
				TreePath[] paths = evt.getPaths();

				// Iterate through all affected nodes
				for (int i = 0; i < paths.length; i++) {
					if (evt.isAddedPath(i)) {
						// This node has been selected
						desktopController.openTree((EntryUiTreeNode) paths[i].getLastPathComponent());
						break;
					}
				}
			}
		});

		panelSearch = new SearchPanel(this);
		contentPane.add(panelSearch, SEARCH_PANEL_NAME);
	}

	private void adjustActionMenus(EntryUiModel entry) {
		mntmExport.setEnabled(entry != null && !entry.isDirectory());

		mntmPaste.setEnabled(entry == null || entry.isDirectory());
	}

	protected void startSearch() {
		panelSearch.resetSearch();
		panelSearch.setSearchTextAndContext(textFieldFind.getText(), desktopController.getCurrentFolderAsString());

		showCardLayoutPanel(SEARCH_PANEL_NAME);
	}

	public void showCardLayoutPanel(String panelName) {
		CardLayout cl = (CardLayout) (contentPane.getLayout());
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
	private void removeKeysFromJTableInputMap(JTable tableFolderEntries2) {
		KeyStroke f2KeyToRemove = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
		KeyStroke deleteKeyToRemove = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		KeyStroke ctrlC = KeyStroke.getKeyStroke('C', KeyEvent.CTRL_DOWN_MASK);
		KeyStroke ctrlX = KeyStroke.getKeyStroke('X', KeyEvent.CTRL_DOWN_MASK);
		KeyStroke ctrlV = KeyStroke.getKeyStroke('V', KeyEvent.CTRL_DOWN_MASK);

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

	protected void startRename() {
		int currentRow = tableFolderEntries.getSelectedRow();
		if (currentRow < 0) {
			JOptionPane.showMessageDialog(this, "Selecte File or Folder", "Badger Message", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		EntryUiModel entry = (EntryUiModel) tableFolderEntries.getModel().getValueAt(currentRow, 0);
		LOGGER.debug("Start renaming " + entry);

		entryCellEditor.setAllowEditing(true);
		tableFolderEntries.editCellAt(currentRow, 0);
	}

	protected void startDelete() {
		int currentRow = tableFolderEntries.getSelectedRow();
		if (currentRow < 0) {
			JOptionPane.showMessageDialog(this, "Select File or Folder", "Badger Message", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		EntryUiModel entry = (EntryUiModel) tableFolderEntries.getModel().getValueAt(currentRow, 0);
		desktopController.startDelete(entry, currentRow);
	}

	/**
	 * helper method for anonymous inner classes (ActionListenerImpl.) to get "this"
	 * 
	 * @return
	 */
	private JFrame getDesktopFrame() {
		return this;
	}

	private void doContextMenuOnEntry(MouseEvent e, final EntryUiModel entry) {
		LOGGER.debug("Opening popup on entry: " + entry.getFullPath());
		JPopupMenu menu = new JPopupMenu();
		if (entry.isDirectory()) {

			JMenuItem mntmNewFolder = new JMenuItem("New Folder");
			mntmNewFolder.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					desktopController.createNewFolderFromContextMenu(entry);
				}
			});
			menu.add(mntmNewFolder);

			JMenuItem mntmPaste = new JMenuItem("Paste");
			// mntmPaste.setAccelerator(KeyStroke.getKeyStroke('V', InputEvent.CTRL_DOWN_MASK));
			mntmPaste.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					desktopController.pasteFromClipboardTo(entry);

				}
			});
			menu.add(mntmPaste);

			JMenuItem mntmImport = new JMenuItem("Import");
			mntmImport.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					desktopController.importFromContextMenu(entry, getDesktopFrame());
				}
			});
			menu.add(mntmImport);

		}

		menu.addSeparator();
		JMenuItem mntmRename = new JMenuItem("Rename");
		mntmRename.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		mntmRename.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LOGGER.debug("RENAME ON ENTRY: " + entry.getFullPath());
				try {
					startRename();
				} catch (Exception ex) {
					SwingUtil.handleException(getDesktopFrame(), ex);
				}
			}
		});
		menu.add(mntmRename);

		JMenuItem mntmDelete = new JMenuItem("Delete");
		mntmDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		mntmDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LOGGER.debug("DELET ON ENTRY: " + entry.getFullPath());
				try {
					startDelete();
				} catch (Exception ex) {
					SwingUtil.handleException(getDesktopFrame(), ex);
				}
			}
		});
		menu.add(mntmDelete);

		JMenuItem mntmImport = new JMenuItem("Export");
		mntmImport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				desktopController.startExport(getDesktopFrame(), entry);
			}
		});
		menu.add(mntmImport);

		JMenuItem mntmCopy = new JMenuItem("Copy");
		// mntmCopy.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK));
		mntmCopy.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				desktopController.copyToClipboard(entry);
			}
		});
		menu.add(mntmCopy);

		JMenuItem mntmCut = new JMenuItem("Cut");
		// mntmCut.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_DOWN_MASK));
		mntmCut.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				desktopController.cutToClipboard(entry);

			}
		});
		menu.add(mntmCut);

		menu.show(tableFolderEntries.getComponentAt(e.getPoint()), e.getX(), e.getY());

	}

	private void beforeWindowClosing() {
		if (!desktopController.isInManagementMode()) {
			Object[] options = new Object[] { "Yes", "No" };
			int retVal = JOptionPane.showOptionDialog(getDesktopFrame(), "Disk is still opened. Do you want me to close it before we exit?", "Close Disk?",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (retVal == JOptionPane.YES_OPTION) {
				try {
					desktopController.closeDisk(getDesktopFrame());
				} catch (VFSException e) {
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

		boolean diskMode = !desktopController.isInManagementMode();

		mnActions.setEnabled(diskMode && (!searching));
		mntmClose.setEnabled(diskMode);
		mntmNew.setEnabled(!diskMode);
		mntmOpen.setEnabled(!diskMode);
		mntmQueryDiskspace.setEnabled(diskMode);

		contentPane.setVisible(diskMode);
		contentPane.setEnabled(diskMode);

		btnSearch.setEnabled(diskMode && (!searching));
		textFieldFind.setEnabled(!searching);
		textFieldCurrentPath.setText(desktopController.getCurrentFolderAsString());
	}

}
