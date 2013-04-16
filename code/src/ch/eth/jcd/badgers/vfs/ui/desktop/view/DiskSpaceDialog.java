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
	private final JTextField textFieldMaxSpace;
	private final JTextField textFieldFreeSpace;
	private final JLabel lblStatus;

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
			GridBagLayout gblPanel = new GridBagLayout();
			gblPanel.columnWidths = new int[] { 0, 0, 0 };
			gblPanel.rowHeights = new int[] { 0, 0, 0 };
			gblPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gblPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
			panel.setLayout(gblPanel);
			{
				JLabel lblMaximumSpace = new JLabel("Maximum Space");
				GridBagConstraints gbcLblMaximumSpace = new GridBagConstraints();
				gbcLblMaximumSpace.anchor = GridBagConstraints.EAST;
				gbcLblMaximumSpace.insets = new Insets(0, 0, 5, 5);
				gbcLblMaximumSpace.gridx = 0;
				gbcLblMaximumSpace.gridy = 0;
				panel.add(lblMaximumSpace, gbcLblMaximumSpace);
			}
			{
				textFieldMaxSpace = new JTextField();
				textFieldMaxSpace.setEditable(false);
				GridBagConstraints gbcTextFieldMaxSpace = new GridBagConstraints();
				gbcTextFieldMaxSpace.insets = new Insets(0, 0, 5, 0);
				gbcTextFieldMaxSpace.fill = GridBagConstraints.HORIZONTAL;
				gbcTextFieldMaxSpace.gridx = 1;
				gbcTextFieldMaxSpace.gridy = 0;
				panel.add(textFieldMaxSpace, gbcTextFieldMaxSpace);
				textFieldMaxSpace.setColumns(10);
			}
			{
				JLabel lblFreeSpace = new JLabel("Free Space");
				GridBagConstraints gbcLblFreeSpace = new GridBagConstraints();
				gbcLblFreeSpace.anchor = GridBagConstraints.EAST;
				gbcLblFreeSpace.insets = new Insets(0, 0, 0, 5);
				gbcLblFreeSpace.gridx = 0;
				gbcLblFreeSpace.gridy = 1;
				panel.add(lblFreeSpace, gbcLblFreeSpace);
			}
			{
				textFieldFreeSpace = new JTextField();
				textFieldFreeSpace.setEditable(false);
				GridBagConstraints gbcTextFieldFreeSpace = new GridBagConstraints();
				gbcTextFieldFreeSpace.fill = GridBagConstraints.HORIZONTAL;
				gbcTextFieldFreeSpace.gridx = 1;
				gbcTextFieldFreeSpace.gridy = 1;
				panel.add(textFieldFreeSpace, gbcTextFieldFreeSpace);
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
					@Override
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
