package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class RemoteDiskDialog extends JDialog {

	private static final long serialVersionUID = 6008623672955958103L;
	private final BadgerMainFrame parent;
	private JTable table;
	private JButton btnCreateNewDisk;

	/**
	 * Create the dialog.
	 */
	public RemoteDiskDialog(JFrame owner) {
		super(owner, true);
		parent = (BadgerMainFrame) owner;
		setTitle("My Remote Disks");
		setBounds(100, 100, 650, 400);
		getContentPane().setLayout(new BorderLayout());
		{
			{
				JPanel panel = new JPanel();
				panel.setBorder(new EmptyBorder(5, 5, 5, 5));
				getContentPane().add(panel, BorderLayout.CENTER);
				panel.setLayout(new BorderLayout(0, 0));
				{
					Box verticalBox = Box.createVerticalBox();
					panel.add(verticalBox);
					// add(new JScrollPane(scrTbl));
					{
						table = new JTable();
						table.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Filename", "Size", "Encrypted", "Compression" }) {
							private static final long serialVersionUID = 1L;
							Class<?>[] columnTypes = new Class[] { String.class, String.class, String.class, Object.class };

							@Override
							public Class<?> getColumnClass(int columnIndex) {
								return columnTypes[columnIndex];
							}
						});
						table.getColumnModel().getColumn(0).setPreferredWidth(125);
						table.getColumnModel().getColumn(1).setPreferredWidth(50);
						table.getColumnModel().getColumn(2).setPreferredWidth(50);
						table.getColumnModel().getColumn(3).setPreferredWidth(50);
					}
					JScrollPane scrollPane = new JScrollPane(table);
					verticalBox.add(scrollPane);
					scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
					Dimension d = table.getPreferredSize();
					scrollPane.setPreferredSize(new Dimension(d.width, table.getRowHeight() * 3));
					{
						btnCreateNewDisk = new JButton("Create new disk");
						btnCreateNewDisk.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg0) {
								dispose();
								parent.getController().openCreateNewRemoteDiskDialog(parent);
							}
						});
						btnCreateNewDisk.setMnemonic('n');
						btnCreateNewDisk.setActionCommand("Create");
						verticalBox.add(btnCreateNewDisk);
					}
				}
			}
			{
				JPanel buttonPane = new JPanel();
				buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
				getContentPane().add(buttonPane, BorderLayout.SOUTH);
				{
					{
						final JButton btnCancel = new JButton("Cancel");
						btnCancel.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(final ActionEvent arg0) {
								dispose();
							}
						});
						btnCancel.setMnemonic('c');
						btnCancel.setActionCommand("Cancel");
						buttonPane.add(btnCancel);
					}

					JButton openButton = new JButton("Open");
					openButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							dispose();
						}
					});
					openButton.setMnemonic('o');
					openButton.setActionCommand("Open");
					buttonPane.add(openButton);
					getRootPane().setDefaultButton(openButton);
				}
			}
		}

	}
}
