package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;

public class VFSSwingGui extends JFrame {

	private final JPanel contentPane;
	private final JTextField txtFind;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					VFSSwingGui frame = new VFSSwingGui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public VFSSwingGui() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 300);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmCreate = new JMenuItem("Create");
		mnFile.add(mntmCreate);

		JMenuItem mntmOpen = new JMenuItem("Open");
		mnFile.add(mntmOpen);

		JMenuItem mntmClose = new JMenuItem("Close");
		mnFile.add(mntmClose);

		JMenuItem mntmDispose = new JMenuItem("Dispose");
		mnFile.add(mntmDispose);

		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);

		JMenuItem mntmCreateNewDirectory = new JMenuItem("Create New Directory");
		mnEdit.add(mntmCreateNewDirectory);

		JMenuItem mntmCreateNewFile = new JMenuItem("Create New File");
		mnEdit.add(mntmCreateNewFile);

		JMenuItem mntmCopyto = new JMenuItem("CopyTo");
		mnEdit.add(mntmCopyto);

		JMenuItem mntmMoveto = new JMenuItem("MoveTo");
		mnEdit.add(mntmMoveto);

		JMenuItem mntmDelete = new JMenuItem("Delete");
		mnEdit.add(mntmDelete);

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
	}

}
