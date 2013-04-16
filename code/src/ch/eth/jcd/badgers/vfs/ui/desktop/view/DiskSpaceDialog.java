package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.BadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.QueryDiskSpaceAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.WorkerController;

public class DiskSpaceDialog extends JDialog implements ActionObserver {

	private static final long serialVersionUID = 6008623672955958103L;

	private final JPanel contentPanel = new JPanel();
	private JTextField textFieldMaxSpace;
	private JTextField textFieldFreeSpace;
	private JLabel lblStatus;

	/**
	 * Create the dialog.
	 */
	public DiskSpaceDialog(JFrame owner) {
		super(owner, true);
		setTitle("Disk Space");
		setBounds(100, 100, 450, 180);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[] { 0, 0, 0 };
			gbl_panel.rowHeights = new int[] { 0, 0, 0 };
			gbl_panel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
			panel.setLayout(gbl_panel);
			{
				JLabel lblMaximumSpace = new JLabel("Maximum Space");
				GridBagConstraints gbc_lblMaximumSpace = new GridBagConstraints();
				gbc_lblMaximumSpace.anchor = GridBagConstraints.EAST;
				gbc_lblMaximumSpace.insets = new Insets(0, 0, 5, 5);
				gbc_lblMaximumSpace.gridx = 0;
				gbc_lblMaximumSpace.gridy = 0;
				panel.add(lblMaximumSpace, gbc_lblMaximumSpace);
			}
			{
				textFieldMaxSpace = new JTextField();
				textFieldMaxSpace.setEditable(false);
				GridBagConstraints gbc_textFieldMaxSpace = new GridBagConstraints();
				gbc_textFieldMaxSpace.insets = new Insets(0, 0, 5, 0);
				gbc_textFieldMaxSpace.fill = GridBagConstraints.HORIZONTAL;
				gbc_textFieldMaxSpace.gridx = 1;
				gbc_textFieldMaxSpace.gridy = 0;
				panel.add(textFieldMaxSpace, gbc_textFieldMaxSpace);
				textFieldMaxSpace.setColumns(10);
			}
			{
				JLabel lblFreeSpace = new JLabel("Free Space");
				GridBagConstraints gbc_lblFreeSpace = new GridBagConstraints();
				gbc_lblFreeSpace.anchor = GridBagConstraints.EAST;
				gbc_lblFreeSpace.insets = new Insets(0, 0, 0, 5);
				gbc_lblFreeSpace.gridx = 0;
				gbc_lblFreeSpace.gridy = 1;
				panel.add(lblFreeSpace, gbc_lblFreeSpace);
			}
			{
				textFieldFreeSpace = new JTextField();
				textFieldFreeSpace.setEditable(false);
				GridBagConstraints gbc_textFieldFreeSpace = new GridBagConstraints();
				gbc_textFieldFreeSpace.fill = GridBagConstraints.HORIZONTAL;
				gbc_textFieldFreeSpace.gridx = 1;
				gbc_textFieldFreeSpace.gridy = 1;
				panel.add(textFieldFreeSpace, gbc_textFieldFreeSpace);
				textFieldFreeSpace.setColumns(10);
			}
		}
		{
			lblStatus = new JLabel("status");
			contentPanel.add(lblStatus, BorderLayout.SOUTH);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}

		startDiskSpaceQuery();
	}

	private void startDiskSpaceQuery() {
		lblStatus.setText("Querying...");

		QueryDiskSpaceAction action = new QueryDiskSpaceAction(this);
		WorkerController.getInstance().enqueue(action);
	}

	@Override
	public void onActionFailed(BadgerAction action, VFSException e) {
		lblStatus.setText("Failed " + e.getMessage());
	}

	@Override
	public void onActionFinished(BadgerAction action) {
		QueryDiskSpaceAction queryAction = (QueryDiskSpaceAction) action;

		lblStatus.setText("");
		textFieldFreeSpace.setText("" + queryAction.getFreeSpace());
		textFieldMaxSpace.setText("" + queryAction.getMaxSpace());
	}

}
