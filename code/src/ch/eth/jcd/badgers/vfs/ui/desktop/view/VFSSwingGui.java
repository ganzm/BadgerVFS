package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import ch.eth.jcd.badgers.vfs.ui.desktop.controller.BadgerViewBase;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;

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

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		initApplication(args);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					VFSSwingGui frame = new VFSSwingGui();
					frame.update();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private static void initApplication(String[] args) {
		String log4JConfigurationPath = null;
		for (int i = 0; i < args.length; i++) {

			if ("-l".equals(args[i]) && (i + 1 < args.length)) {

				log4JConfigurationPath = args[i + 1];

			}

		}

		initLog4J(log4JConfigurationPath);
	}

	private static void initLog4J(String log4jConfigurationPath) {
		if (log4jConfigurationPath == null) {
			log4jConfigurationPath = "log4j.xml";

		}
		DOMConfigurator.configure(log4jConfigurationPath);
		LOGGER.info("Log4J initialized with " + log4jConfigurationPath);
	}

	/**
	 * Create the frame.
	 */
	public VFSSwingGui() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 300);

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
		mnDisk.add(mntmOpen);

		mntmClose = new JMenuItem("Close");
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
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JTree tree = new JTree();
		contentPane.add(tree, BorderLayout.WEST);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);

		btnTestBlockingAction = new JButton("Test Blocking Action");
		btnTestBlockingAction.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				desktopController.testBlockingAction();
			}
		});
		panel.add(btnTestBlockingAction);
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

	}
}
