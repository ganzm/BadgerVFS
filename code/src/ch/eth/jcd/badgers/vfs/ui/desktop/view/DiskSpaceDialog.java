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
import javax.swing.border.TitledBorder;

import ch.eth.jcd.badgers.vfs.core.model.DiskSpaceUsage;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.QueryDiskInfoAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;

public class DiskSpaceDialog extends JDialog implements ActionObserver {

	private static final int FIRST_COLUMN_WIDTH = 120;

	private static final long serialVersionUID = 6008623672955958103L;

	private final JPanel contentPanel = new JPanel();
	private final JTextField textFieldMaxSpace;
	private final JTextField textFieldFreeSpace;
	private final JLabel lblStatus;
	private final JProgressBar progressBarDiskSpace;
	private final JTextField textFieldDirectoryBlocks;
	private final DesktopController desktopController;
	private final JTextField textFieldDiskId;
	private final JTextField textFieldHost;
	private final JTextField textFieldVersion;

	/**
	 * Create the dialog.
	 */
	public DiskSpaceDialog(final DesktopController desktopController) {
		super((BadgerMainFrame) desktopController.getView(), true);
		this.desktopController = desktopController;
		setTitle("Disk Info");
		setBounds(100, 100, 450, 427);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gblContentPanel = new GridBagLayout();
		gblContentPanel.columnWidths = new int[] { 424, 0 };
		gblContentPanel.rowHeights = new int[] { 113, 113, 0, 0 };
		gblContentPanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gblContentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gblContentPanel);
		{
			final JPanel panelDiskSpace = new JPanel();
			panelDiskSpace.setBorder(new TitledBorder(null, "Disk Space", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			GridBagConstraints gbcPanelDiskSpace = new GridBagConstraints();
			gbcPanelDiskSpace.fill = GridBagConstraints.BOTH;
			gbcPanelDiskSpace.insets = new Insets(0, 0, 5, 0);
			gbcPanelDiskSpace.gridx = 0;
			gbcPanelDiskSpace.gridy = 0;
			contentPanel.add(panelDiskSpace, gbcPanelDiskSpace);
			final GridBagLayout gblPanelDiskSpace = new GridBagLayout();
			gblPanelDiskSpace.columnWidths = new int[] { FIRST_COLUMN_WIDTH, 0, 0 };
			gblPanelDiskSpace.rowHeights = new int[] { 0, 0, 0, 0, 0 };
			gblPanelDiskSpace.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gblPanelDiskSpace.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
			panelDiskSpace.setLayout(gblPanelDiskSpace);
			{
				final JLabel lblMaximumSpace = new JLabel("Maximum Space");
				final GridBagConstraints gbcLblMaximumSpace = new GridBagConstraints();
				gbcLblMaximumSpace.anchor = GridBagConstraints.EAST;
				gbcLblMaximumSpace.insets = new Insets(0, 0, 5, 5);
				gbcLblMaximumSpace.gridx = 0;
				gbcLblMaximumSpace.gridy = 0;
				panelDiskSpace.add(lblMaximumSpace, gbcLblMaximumSpace);
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
				panelDiskSpace.add(textFieldMaxSpace, gbcTextFieldMaxSpace);
				textFieldMaxSpace.setColumns(10);
			}
			{
				final JLabel lblFreeSpace = new JLabel("Free Space");
				final GridBagConstraints gbcLblFreeSpace = new GridBagConstraints();
				gbcLblFreeSpace.anchor = GridBagConstraints.EAST;
				gbcLblFreeSpace.insets = new Insets(0, 0, 5, 5);
				gbcLblFreeSpace.gridx = 0;
				gbcLblFreeSpace.gridy = 1;
				panelDiskSpace.add(lblFreeSpace, gbcLblFreeSpace);
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
				panelDiskSpace.add(textFieldFreeSpace, gbcTextFieldFreeSpace);
				textFieldFreeSpace.setColumns(10);
			}
			{
				progressBarDiskSpace = new JProgressBar();
				final GridBagConstraints gbcProgressBarDiskSpace = new GridBagConstraints();
				gbcProgressBarDiskSpace.insets = new Insets(0, 0, 5, 0);
				gbcProgressBarDiskSpace.fill = GridBagConstraints.BOTH;
				gbcProgressBarDiskSpace.gridx = 1;
				gbcProgressBarDiskSpace.gridy = 2;
				panelDiskSpace.add(progressBarDiskSpace, gbcProgressBarDiskSpace);
			}
			{
				final JLabel lblDirectoryBlockUsage = new JLabel("Directory Block usage");
				final GridBagConstraints gbcLblDirectoryBlockUsage = new GridBagConstraints();
				gbcLblDirectoryBlockUsage.anchor = GridBagConstraints.EAST;
				gbcLblDirectoryBlockUsage.insets = new Insets(0, 0, 0, 5);
				gbcLblDirectoryBlockUsage.gridx = 0;
				gbcLblDirectoryBlockUsage.gridy = 3;
				panelDiskSpace.add(lblDirectoryBlockUsage, gbcLblDirectoryBlockUsage);
			}
			{
				textFieldDirectoryBlocks = new JTextField();
				textFieldDirectoryBlocks.setHorizontalAlignment(SwingConstants.RIGHT);
				textFieldDirectoryBlocks.setEditable(false);
				final GridBagConstraints gbcTextFieldDirectoryBlocks = new GridBagConstraints();
				gbcTextFieldDirectoryBlocks.fill = GridBagConstraints.HORIZONTAL;
				gbcTextFieldDirectoryBlocks.gridx = 1;
				gbcTextFieldDirectoryBlocks.gridy = 3;
				panelDiskSpace.add(textFieldDirectoryBlocks, gbcTextFieldDirectoryBlocks);
				textFieldDirectoryBlocks.setColumns(10);
			}
		}
		{
			JPanel panelSynchronisation = new JPanel();
			panelSynchronisation.setBorder(new TitledBorder(null, "Synchronisation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			GridBagConstraints gbcPanelSynchronisation = new GridBagConstraints();
			gbcPanelSynchronisation.insets = new Insets(0, 0, 5, 0);
			gbcPanelSynchronisation.anchor = GridBagConstraints.NORTH;
			gbcPanelSynchronisation.fill = GridBagConstraints.HORIZONTAL;
			gbcPanelSynchronisation.gridx = 0;
			gbcPanelSynchronisation.gridy = 1;
			contentPanel.add(panelSynchronisation, gbcPanelSynchronisation);
			GridBagLayout gblPanelSynchronisation = new GridBagLayout();
			gblPanelSynchronisation.columnWidths = new int[] { FIRST_COLUMN_WIDTH, 0, 0 };
			gblPanelSynchronisation.rowHeights = new int[] { 0, 0, 0, 0 };
			gblPanelSynchronisation.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gblPanelSynchronisation.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
			panelSynchronisation.setLayout(gblPanelSynchronisation);
			{
				JLabel lblDiskId = new JLabel("Disk Id");
				GridBagConstraints gbcLblDiskId = new GridBagConstraints();
				gbcLblDiskId.insets = new Insets(0, 0, 5, 5);
				gbcLblDiskId.anchor = GridBagConstraints.EAST;
				gbcLblDiskId.gridx = 0;
				gbcLblDiskId.gridy = 0;
				panelSynchronisation.add(lblDiskId, gbcLblDiskId);
			}
			{
				textFieldDiskId = new JTextField();
				textFieldDiskId.setEditable(false);
				GridBagConstraints gbcTextFieldDiskId = new GridBagConstraints();
				gbcTextFieldDiskId.insets = new Insets(0, 0, 5, 0);
				gbcTextFieldDiskId.fill = GridBagConstraints.HORIZONTAL;
				gbcTextFieldDiskId.gridx = 1;
				gbcTextFieldDiskId.gridy = 0;
				panelSynchronisation.add(textFieldDiskId, gbcTextFieldDiskId);
				textFieldDiskId.setColumns(10);
			}
			{
				JLabel lblHost = new JLabel("Host");
				GridBagConstraints gbcLblHost = new GridBagConstraints();
				gbcLblHost.anchor = GridBagConstraints.EAST;
				gbcLblHost.insets = new Insets(0, 0, 5, 5);
				gbcLblHost.gridx = 0;
				gbcLblHost.gridy = 1;
				panelSynchronisation.add(lblHost, gbcLblHost);
			}
			{
				textFieldHost = new JTextField();
				textFieldHost.setEditable(false);
				GridBagConstraints gbcTextFieldHost = new GridBagConstraints();
				gbcTextFieldHost.insets = new Insets(0, 0, 5, 0);
				gbcTextFieldHost.fill = GridBagConstraints.HORIZONTAL;
				gbcTextFieldHost.gridx = 1;
				gbcTextFieldHost.gridy = 1;
				panelSynchronisation.add(textFieldHost, gbcTextFieldHost);
				textFieldHost.setColumns(10);
			}
			{
				JLabel lblVersion = new JLabel("Version");
				GridBagConstraints gbcLblVersion = new GridBagConstraints();
				gbcLblVersion.anchor = GridBagConstraints.EAST;
				gbcLblVersion.insets = new Insets(0, 0, 0, 5);
				gbcLblVersion.gridx = 0;
				gbcLblVersion.gridy = 2;
				panelSynchronisation.add(lblVersion, gbcLblVersion);
			}
			{
				textFieldVersion = new JTextField();
				textFieldVersion.setEditable(false);
				GridBagConstraints gbcTextFieldVersion = new GridBagConstraints();
				gbcTextFieldVersion.fill = GridBagConstraints.HORIZONTAL;
				gbcTextFieldVersion.gridx = 1;
				gbcTextFieldVersion.gridy = 2;
				panelSynchronisation.add(textFieldVersion, gbcTextFieldVersion);
				textFieldVersion.setColumns(10);
			}
		}
		{
			lblStatus = new JLabel("status");
			GridBagConstraints gbcLblStatus = new GridBagConstraints();
			gbcLblStatus.anchor = GridBagConstraints.NORTH;
			gbcLblStatus.fill = GridBagConstraints.HORIZONTAL;
			gbcLblStatus.gridx = 0;
			gbcLblStatus.gridy = 2;
			contentPanel.add(lblStatus, gbcLblStatus);
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

		final QueryDiskInfoAction action = new QueryDiskInfoAction(this);
		desktopController.getWorkerController().enqueue(action);
	}

	@Override
	public void onActionFailed(final AbstractBadgerAction action, final Exception e) {
		lblStatus.setText("Failed " + e.getMessage());
	}

	@Override
	public void onActionFinished(final AbstractBadgerAction action) {
		final QueryDiskInfoAction queryAction = (QueryDiskInfoAction) action;

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

		textFieldDiskId.setText(queryAction.getDiskId().toString());
		textFieldVersion.setText(queryAction.getServerVersion() + "");
		textFieldHost.setText(queryAction.getLinkedHostname());
	}

}
