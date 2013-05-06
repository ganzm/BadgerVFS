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
	private JTextField textFieldDiskId;
	private JTextField textFieldHost;
	private JTextField textFieldVersion;

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
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 424, 0 };
		gbl_contentPanel.rowHeights = new int[] { 113, 113, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);
		{
			final JPanel panelDiskSpace = new JPanel();
			panelDiskSpace.setBorder(new TitledBorder(null, "Disk Space", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			GridBagConstraints gbc_panelDiskSpace = new GridBagConstraints();
			gbc_panelDiskSpace.fill = GridBagConstraints.BOTH;
			gbc_panelDiskSpace.insets = new Insets(0, 0, 5, 0);
			gbc_panelDiskSpace.gridx = 0;
			gbc_panelDiskSpace.gridy = 0;
			contentPanel.add(panelDiskSpace, gbc_panelDiskSpace);
			final GridBagLayout gbl_panelDiskSpace = new GridBagLayout();
			gbl_panelDiskSpace.columnWidths = new int[] { FIRST_COLUMN_WIDTH, 0, 0 };
			gbl_panelDiskSpace.rowHeights = new int[] { 0, 0, 0, 0, 0 };
			gbl_panelDiskSpace.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gbl_panelDiskSpace.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
			panelDiskSpace.setLayout(gbl_panelDiskSpace);
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
			GridBagConstraints gbc_panelSynchronisation = new GridBagConstraints();
			gbc_panelSynchronisation.insets = new Insets(0, 0, 5, 0);
			gbc_panelSynchronisation.anchor = GridBagConstraints.NORTH;
			gbc_panelSynchronisation.fill = GridBagConstraints.HORIZONTAL;
			gbc_panelSynchronisation.gridx = 0;
			gbc_panelSynchronisation.gridy = 1;
			contentPanel.add(panelSynchronisation, gbc_panelSynchronisation);
			GridBagLayout gbl_panelSynchronisation = new GridBagLayout();
			gbl_panelSynchronisation.columnWidths = new int[] { FIRST_COLUMN_WIDTH, 0, 0 };
			gbl_panelSynchronisation.rowHeights = new int[] { 0, 0, 0, 0 };
			gbl_panelSynchronisation.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gbl_panelSynchronisation.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
			panelSynchronisation.setLayout(gbl_panelSynchronisation);
			{
				JLabel lblDiskId = new JLabel("Disk Id");
				GridBagConstraints gbc_lblDiskId = new GridBagConstraints();
				gbc_lblDiskId.insets = new Insets(0, 0, 5, 5);
				gbc_lblDiskId.anchor = GridBagConstraints.EAST;
				gbc_lblDiskId.gridx = 0;
				gbc_lblDiskId.gridy = 0;
				panelSynchronisation.add(lblDiskId, gbc_lblDiskId);
			}
			{
				textFieldDiskId = new JTextField();
				textFieldDiskId.setEditable(false);
				GridBagConstraints gbc_textFieldDiskId = new GridBagConstraints();
				gbc_textFieldDiskId.insets = new Insets(0, 0, 5, 0);
				gbc_textFieldDiskId.fill = GridBagConstraints.HORIZONTAL;
				gbc_textFieldDiskId.gridx = 1;
				gbc_textFieldDiskId.gridy = 0;
				panelSynchronisation.add(textFieldDiskId, gbc_textFieldDiskId);
				textFieldDiskId.setColumns(10);
			}
			{
				JLabel lblHost = new JLabel("Host");
				GridBagConstraints gbc_lblHost = new GridBagConstraints();
				gbc_lblHost.anchor = GridBagConstraints.EAST;
				gbc_lblHost.insets = new Insets(0, 0, 5, 5);
				gbc_lblHost.gridx = 0;
				gbc_lblHost.gridy = 1;
				panelSynchronisation.add(lblHost, gbc_lblHost);
			}
			{
				textFieldHost = new JTextField();
				textFieldHost.setEditable(false);
				GridBagConstraints gbc_textFieldHost = new GridBagConstraints();
				gbc_textFieldHost.insets = new Insets(0, 0, 5, 0);
				gbc_textFieldHost.fill = GridBagConstraints.HORIZONTAL;
				gbc_textFieldHost.gridx = 1;
				gbc_textFieldHost.gridy = 1;
				panelSynchronisation.add(textFieldHost, gbc_textFieldHost);
				textFieldHost.setColumns(10);
			}
			{
				JLabel lblVersion = new JLabel("Version");
				GridBagConstraints gbc_lblVersion = new GridBagConstraints();
				gbc_lblVersion.anchor = GridBagConstraints.EAST;
				gbc_lblVersion.insets = new Insets(0, 0, 0, 5);
				gbc_lblVersion.gridx = 0;
				gbc_lblVersion.gridy = 2;
				panelSynchronisation.add(lblVersion, gbc_lblVersion);
			}
			{
				textFieldVersion = new JTextField();
				textFieldVersion.setEditable(false);
				GridBagConstraints gbc_textFieldVersion = new GridBagConstraints();
				gbc_textFieldVersion.fill = GridBagConstraints.HORIZONTAL;
				gbc_textFieldVersion.gridx = 1;
				gbc_textFieldVersion.gridy = 2;
				panelSynchronisation.add(textFieldVersion, gbc_textFieldVersion);
				textFieldVersion.setColumns(10);
			}
		}
		{
			lblStatus = new JLabel("status");
			GridBagConstraints gbc_lblStatus = new GridBagConstraints();
			gbc_lblStatus.anchor = GridBagConstraints.NORTH;
			gbc_lblStatus.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblStatus.gridx = 0;
			gbc_lblStatus.gridy = 2;
			contentPanel.add(lblStatus, gbc_lblStatus);
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
