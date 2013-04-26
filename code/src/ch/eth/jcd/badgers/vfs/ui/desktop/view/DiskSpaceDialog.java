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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import ch.eth.jcd.badgers.vfs.core.model.DiskSpaceUsage;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.QueryDiskSpaceAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;

public class DiskSpaceDialog extends JDialog implements ActionObserver {

	private static final long serialVersionUID = 6008623672955958103L;

	private final JPanel contentPanel = new JPanel();
	private final JTextField textFieldMaxSpace;
	private final JTextField textFieldFreeSpace;
	private final JLabel lblStatus;
	private final JProgressBar progressBarDiskSpace;
	private final JTextField textFieldDirectoryBlocks;
	private final DesktopController desktopController;

	/**
	 * Create the dialog.
	 */
	public DiskSpaceDialog(final DesktopController desktopController) {
		super((BadgerMainFrame) desktopController.getView(), true);
		this.desktopController = desktopController;
		setTitle("Disk Space");
		setBounds(100, 100, 450, 215);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			final JPanel panel = new JPanel();
			contentPanel.add(panel);
			final GridBagLayout gblPanel = new GridBagLayout();
			gblPanel.columnWidths = new int[] { 0, 0, 0 };
			gblPanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
			gblPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gblPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
			panel.setLayout(gblPanel);
			{
				final JLabel lblMaximumSpace = new JLabel("Maximum Space");
				final GridBagConstraints gbcLblMaximumSpace = new GridBagConstraints();
				gbcLblMaximumSpace.anchor = GridBagConstraints.EAST;
				gbcLblMaximumSpace.insets = new Insets(0, 0, 5, 5);
				gbcLblMaximumSpace.gridx = 0;
				gbcLblMaximumSpace.gridy = 0;
				panel.add(lblMaximumSpace, gbcLblMaximumSpace);
			}
			{
				textFieldMaxSpace = new JTextField();
				textFieldMaxSpace.setHorizontalAlignment(SwingConstants.RIGHT);
				textFieldMaxSpace.setEditable(false);
				final GridBagConstraints gbcTextFieldMaxSpace = new GridBagConstraints();
				gbcTextFieldMaxSpace.insets = new Insets(0, 0, 5, 0);
				gbcTextFieldMaxSpace.fill = GridBagConstraints.HORIZONTAL;
				gbcTextFieldMaxSpace.gridx = 1;
				gbcTextFieldMaxSpace.gridy = 0;
				panel.add(textFieldMaxSpace, gbcTextFieldMaxSpace);
				textFieldMaxSpace.setColumns(10);
			}
			{
				final JLabel lblFreeSpace = new JLabel("Free Space");
				final GridBagConstraints gbcLblFreeSpace = new GridBagConstraints();
				gbcLblFreeSpace.anchor = GridBagConstraints.EAST;
				gbcLblFreeSpace.insets = new Insets(0, 0, 5, 5);
				gbcLblFreeSpace.gridx = 0;
				gbcLblFreeSpace.gridy = 1;
				panel.add(lblFreeSpace, gbcLblFreeSpace);
			}
			{
				textFieldFreeSpace = new JTextField();
				textFieldFreeSpace.setHorizontalAlignment(SwingConstants.RIGHT);
				textFieldFreeSpace.setEditable(false);
				final GridBagConstraints gbcTextFieldFreeSpace = new GridBagConstraints();
				gbcTextFieldFreeSpace.insets = new Insets(0, 0, 5, 0);
				gbcTextFieldFreeSpace.fill = GridBagConstraints.HORIZONTAL;
				gbcTextFieldFreeSpace.gridx = 1;
				gbcTextFieldFreeSpace.gridy = 1;
				panel.add(textFieldFreeSpace, gbcTextFieldFreeSpace);
				textFieldFreeSpace.setColumns(10);
			}
			{
				progressBarDiskSpace = new JProgressBar();
				final GridBagConstraints gbcProgressBarDiskSpace = new GridBagConstraints();
				gbcProgressBarDiskSpace.insets = new Insets(0, 0, 5, 0);
				gbcProgressBarDiskSpace.fill = GridBagConstraints.BOTH;
				gbcProgressBarDiskSpace.gridx = 1;
				gbcProgressBarDiskSpace.gridy = 2;
				panel.add(progressBarDiskSpace, gbcProgressBarDiskSpace);
			}
			{
				final JLabel lblDirectoryBlockUsage = new JLabel("Directory Block usage");
				final GridBagConstraints gbcLblDirectoryBlockUsage = new GridBagConstraints();
				gbcLblDirectoryBlockUsage.anchor = GridBagConstraints.EAST;
				gbcLblDirectoryBlockUsage.insets = new Insets(0, 0, 0, 5);
				gbcLblDirectoryBlockUsage.gridx = 0;
				gbcLblDirectoryBlockUsage.gridy = 3;
				panel.add(lblDirectoryBlockUsage, gbcLblDirectoryBlockUsage);
			}
			{
				textFieldDirectoryBlocks = new JTextField();
				textFieldDirectoryBlocks.setHorizontalAlignment(SwingConstants.RIGHT);
				textFieldDirectoryBlocks.setEditable(false);
				final GridBagConstraints gbcTextFieldDirectoryBlocks = new GridBagConstraints();
				gbcTextFieldDirectoryBlocks.fill = GridBagConstraints.HORIZONTAL;
				gbcTextFieldDirectoryBlocks.gridx = 1;
				gbcTextFieldDirectoryBlocks.gridy = 3;
				panel.add(textFieldDirectoryBlocks, gbcTextFieldDirectoryBlocks);
				textFieldDirectoryBlocks.setColumns(10);
			}
		}
		{
			lblStatus = new JLabel("status");
			contentPanel.add(lblStatus, BorderLayout.SOUTH);
		}
		{
			final JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				final JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent arg0) {
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

		final QueryDiskSpaceAction action = new QueryDiskSpaceAction(this);
		desktopController.getWorkerController().enqueue(action);
	}

	@Override
	public void onActionFailed(final AbstractBadgerAction action, final Exception e) {
		lblStatus.setText("Failed " + e.getMessage());
	}

	@Override
	public void onActionFinished(final AbstractBadgerAction action) {
		final QueryDiskSpaceAction queryAction = (QueryDiskSpaceAction) action;

		final DiskSpaceUsage du = queryAction.getDiskSpaceUsage();

		lblStatus.setText("");
		textFieldFreeSpace.setText(du.getFreeData() + " bytes");
		textFieldMaxSpace.setText(du.getMaxData() + " bytes");

		final long maxMb = du.getMaxData() / 1024 / 1024;
		final long freeMb = du.getFreeData() / 1024 / 1024;
		// progress bar does not like values below 1;
		final int intValue = (int) (maxMb - freeMb);

		progressBarDiskSpace.setStringPainted(true);
		progressBarDiskSpace.setMaximum((int) maxMb);
		progressBarDiskSpace.setValue(intValue);

		textFieldDirectoryBlocks.setText(du.getMaxDirectoryBlocks() - du.getFreeDirectoryBlocks() + "/" + du.getMaxDirectoryBlocks());
	}

}
