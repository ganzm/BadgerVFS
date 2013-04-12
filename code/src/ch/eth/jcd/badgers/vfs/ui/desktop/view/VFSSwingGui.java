package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.ui.desktop.Initialisation;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.BadgerViewBase;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public class VFSSwingGui extends JFrame implements BadgerViewBase {

	private static final long serialVersionUID = -8776317677851635247L;

	private static final Logger LOGGER = Logger.getLogger(VFSSwingGui.class);

	private final JPanel contentPane;
	private final JTextField txtFind;
	private final JButton btnTestBlockingAction;

	private final DesktopController desktopController = new DesktopController(this);
	private final JMenu mnActions;
	private final JMenuItem mntmNew;
	private final JMenuItem mntmOpen;
	private final JMenuItem mntmClose;
	private JTextField textField;

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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		mnActions.add(mntmNewFolder);

		JMenuItem mntmRename = new JMenuItem("Rename");
		mnActions.add(mntmRename);

		JMenuItem mntmDelete = new JMenuItem("Delete");
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

		JList<EntryUiModel> entryList = new JList<EntryUiModel>();
		ListModel<EntryUiModel> entryListModel = desktopController.getEntryListModel();
		entryList.setModel(entryListModel);
		panelMiddle.add(entryList, BorderLayout.CENTER);

		JPanel panelLeft = new JPanel();
		splitPane.setLeftComponent(panelLeft);
		panelLeft.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPaneFolderTree = new JScrollPane();
		panelLeft.add(scrollPaneFolderTree);

		JTree folderTree = new JTree();
		scrollPaneFolderTree.setViewportView(folderTree);

		JPanel panelBottom = new JPanel();
		contentPane.add(panelBottom, BorderLayout.SOUTH);

		btnTestBlockingAction = new JButton("Test Blocking Action");
		panelBottom.add(btnTestBlockingAction);
		btnTestBlockingAction.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				desktopController.testBlockingAction();
			}
		});
	}

	/**
	 * helper method for anonymous inner classes (ActionListenerImpl.) to get "this"
	 * 
	 * 
	 * @return
	 */
	private JFrame getDesktopFrame() {
		return this;
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
}
