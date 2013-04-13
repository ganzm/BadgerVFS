package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
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
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryTableModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiTreeNode;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public class VFSSwingGui extends JFrame implements BadgerViewBase {

	private static final long serialVersionUID = -8776317677851635247L;

	private static final Logger LOGGER = Logger.getLogger(VFSSwingGui.class);

	private final EntryCellEditor entryCellEditor;

	private final JPanel contentPane;
	private final JTextField txtFind;
	private final JButton btnTestBlockingAction;

	private final DesktopController desktopController = new DesktopController(this);
	private final JMenu mnActions;
	private final JMenuItem mntmNew;
	private final JMenuItem mntmOpen;
	private final JMenuItem mntmClose;
	private final JTextField textField;
	private final JTable tableFolderEntries;
	private final JTree folderTree;

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
					e.printStackTrace();
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
				} catch (Exception ex) {
					SwingUtil.handleException(getDesktopFrame(), ex);
				}
			}
		});
		mnDisk.add(mntmClose);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mnDisk.add(mntmExit);

		mnActions = new JMenu("Actions");
		mnActions.setMnemonic('A');
		menuBar.add(mnActions);

		JMenuItem mntmNewFolder = new JMenuItem("New Folder");
		mntmNewFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					desktopController.startCreatenewFolder();
				} catch (Exception ex) {
					SwingUtil.handleException(getDesktopFrame(), ex);
				}
			}
		});
		mnActions.add(mntmNewFolder);

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

		JMenuItem mntmDelete = new JMenuItem("Delete");
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

		JMenuItem mntmNewFile = new JMenuItem("New File");
		mnActions.add(mntmNewFile);

		JMenuItem mntmImport = new JMenuItem("Import");
		mnActions.add(mntmImport);

		JMenuItem mntmExport = new JMenuItem("Export");
		mnActions.add(mntmExport);

		JMenuItem mntmCopyto = new JMenuItem("CopyTo");
		mnActions.add(mntmCopyto);

		JMenuItem mntmMoveto = new JMenuItem("MoveTo");
		mnActions.add(mntmMoveto);

		JMenu mnHelp = new JMenu("Help");
		mnHelp.setMnemonic('H');
		menuBar.add(mnHelp);

		JMenuItem mntmInfo = new JMenuItem("Info");
		mnHelp.add(mntmInfo);

		txtFind = new JTextField();
		txtFind.setText("Find");
		menuBar.add(txtFind);
		txtFind.setColumns(10);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		contentPane.add(splitPane);

		JPanel panelMiddle = new JPanel();
		splitPane.setRightComponent(panelMiddle);
		panelMiddle.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panelMiddle.add(panel, BorderLayout.NORTH);
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

		textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		panel.add(textField, gbc_textField);
		textField.setColumns(10);

		tableFolderEntries = new JTable();
		tableFolderEntries.setShowGrid(false);
		tableFolderEntries.setDefaultRenderer(EntryUiModel.class, new EntryListCellRenderer());
		tableFolderEntries.setRowHeight(40);

		EntryTableModel entryTableModel = desktopController.getEntryTableModel();
		tableFolderEntries.setModel(entryTableModel);

		entryCellEditor = new EntryCellEditor(tableFolderEntries, desktopController);
		TableColumn columnModel = tableFolderEntries.getColumnModel().getColumn(0);
		columnModel.setCellEditor(entryCellEditor);

		panelMiddle.add(tableFolderEntries, BorderLayout.CENTER);

		tableFolderEntries.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() == 2) {
					// doubleclicked on
					EntryUiModel entry = (EntryUiModel) tableFolderEntries.getModel().getValueAt(tableFolderEntries.getSelectedRow(), 0);
					LOGGER.debug("Doubleclicked " + entry);
					if (entry.isDirectory()) {
						desktopController.openEntry(entry);
					}
				}
			}
		});

		performUglyF2KeyStrokeHack(tableFolderEntries);

		JPanel panelLeft = new JPanel();
		splitPane.setLeftComponent(panelLeft);
		panelLeft.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPaneFolderTree = new JScrollPane();
		panelLeft.add(scrollPaneFolderTree);

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("/", true);
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

		JPanel panelBottom = new JPanel();
		contentPane.add(panelBottom, BorderLayout.SOUTH);

		btnTestBlockingAction = new JButton("Test Button");
		panelBottom.add(btnTestBlockingAction);
		btnTestBlockingAction.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				LOGGER.info("Does nothing");
			}
		});
	}

	/**
	 * copied from http://stackoverflow.com/questions/2019371/swing-setting-a-function-key-f2-as-an-accelerator
	 * 
	 * @param tableFolderEntries2
	 */
	private void performUglyF2KeyStrokeHack(JTable tableFolderEntries2) {
		KeyStroke keyToRemove = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);

		InputMap imap = tableFolderEntries.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		while (imap != null) {
			imap.remove(keyToRemove);
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
			JOptionPane.showMessageDialog(this, "Selecte File or Folder", "Badger Message", JOptionPane.INFORMATION_MESSAGE);
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

		btnTestBlockingAction.setEnabled(diskMode);

		mnActions.setEnabled(diskMode);
		mntmClose.setEnabled(diskMode);
		mntmNew.setEnabled(!diskMode);
		mntmOpen.setEnabled(!diskMode);

		contentPane.setVisible(diskMode);
		contentPane.setEnabled(diskMode);
	}

	private void clearTableSelection() {
		tableFolderEntries.clearSelection();
	}
}
