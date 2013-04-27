package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.remote.model.LinkedDiskTableModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.RemoteSynchronisationWizardContext;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public class RemoteDiskDialog extends JDialog {

	private static final long serialVersionUID = 6008623672955958103L;
	private static final Logger LOGGER = Logger.getLogger(RemoteDiskDialog.class);
	private final DesktopController controller;
	private LinkedDiskTableModel linkedDiskTableModel;
	private JTable table;
	private JButton btnCreateNewDisk;

	/**
	 * Create the dialog.
	 * 
	 * @throws RemoteException
	 */
	public RemoteDiskDialog(final DesktopController desktopController, final RemoteSynchronisationWizardContext wizardContext) {
		super((BadgerMainFrame) desktopController.getView(), true);
		controller = desktopController;
		setTitle("My Remote Disks");
		setBounds(100, 100, 650, 400);
		getContentPane().setLayout(new BorderLayout());
		{
			{
				final JPanel panel = new JPanel();
				panel.setBorder(new EmptyBorder(5, 5, 5, 5));
				getContentPane().add(panel, BorderLayout.CENTER);
				panel.setLayout(new BorderLayout(0, 0));
				{
					final Box verticalBox = Box.createVerticalBox();
					panel.add(verticalBox);
					// add(new JScrollPane(scrTbl));
					{
						table = new JTable();
						final Object[][] disks;
						try {
							linkedDiskTableModel = new LinkedDiskTableModel(wizardContext.getRemoteManager().getAdminInterface().listDisks());
							table.setModel(linkedDiskTableModel);
						} catch (final RemoteException e) {
							LOGGER.error(e);
							SwingUtil.handleException(getThis(), e);
							dispose();
						}
						table.getColumnModel().getColumn(0).setPreferredWidth(125);
						table.getColumnModel().getColumn(1).setPreferredWidth(50);
						table.getColumnModel().getColumn(2).setPreferredWidth(50);
						table.getColumnModel().getColumn(3).setPreferredWidth(50);
					}
					final JScrollPane scrollPane = new JScrollPane(table);
					verticalBox.add(scrollPane);
					scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
					final Dimension d = table.getPreferredSize();
					scrollPane.setPreferredSize(new Dimension(d.width, table.getRowHeight() * 3));
					{
						btnCreateNewDisk = new JButton("Create new disk");
						btnCreateNewDisk.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(final ActionEvent arg0) {
								dispose();
								controller.openCreateNewRemoteDiskDialog(wizardContext);
							}
						});
						btnCreateNewDisk.setMnemonic('n');
						btnCreateNewDisk.setActionCommand("Create");
						verticalBox.add(btnCreateNewDisk);
					}
				}
			}
			{
				final JPanel buttonPane = new JPanel();
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

					final JButton openButton = new JButton("Open");
					openButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent arg0) {
							final int selectedRow = table.getSelectedRow();
							if (selectedRow == -1) {
								SwingUtil.handleError(getThis(), "No disk selected");
							} else {
								wizardContext.setSelectedDiskToLink(linkedDiskTableModel.getEntries().get(selectedRow));
								dispose();
								controller.openGetRemoteLinkedDiskDialog(wizardContext);

							}
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

	private RemoteDiskDialog getThis() {
		return this;
	}
}
