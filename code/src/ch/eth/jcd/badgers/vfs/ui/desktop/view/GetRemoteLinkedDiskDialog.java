package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.RemoteSynchronisationWizardContext;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public class GetRemoteLinkedDiskDialog extends JDialog {

	private static final Logger LOGGER = Logger.getLogger(GetRemoteLinkedDiskDialog.class);
	private static final String DEFAULT_FILE_NAME = "disk.bfs";

	private static final long serialVersionUID = -2652867330270571476L;

	private final DesktopController ownerController;

	private final JPanel contentPanel = new JPanel();
	private final JTextField txtPath;

	/**
	 * Create the dialog.
	 */
	public GetRemoteLinkedDiskDialog(final DesktopController ownerController, final RemoteSynchronisationWizardContext wizardContext) {
		super((BadgerMainFrame) ownerController.getView(), "Set filepath for remote disk", true);
		this.ownerController = ownerController;

		setBounds(100, 100, 400, 213);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		final GridBagLayout gblContentPanel = new GridBagLayout();
		gblContentPanel.columnWidths = new int[] { 0, 0, 0, 0 };
		gblContentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gblContentPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gblContentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gblContentPanel);
		{
			final JLabel lblPath = new JLabel("Path");
			final GridBagConstraints gbc_lblPath = new GridBagConstraints();
			gbc_lblPath.anchor = GridBagConstraints.EAST;
			gbc_lblPath.insets = new Insets(0, 0, 5, 5);
			gbc_lblPath.gridx = 0;
			gbc_lblPath.gridy = 0;
			contentPanel.add(lblPath, gbc_lblPath);
		}
		{
			txtPath = new JTextField();
			final GridBagConstraints gbc_txtPath = new GridBagConstraints();
			gbc_txtPath.insets = new Insets(0, 0, 5, 5);
			gbc_txtPath.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtPath.gridx = 1;
			gbc_txtPath.gridy = 0;
			contentPanel.add(txtPath, gbc_txtPath);
			txtPath.setColumns(10);
		}
		{
			final JButton btnShowFileChooser = new JButton("Browse");
			btnShowFileChooser.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final JFileChooser fc = new JFileChooser();
					fc.setDialogTitle("Choose Disk File Path");
					fc.setDialogType(JFileChooser.SAVE_DIALOG);
					fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

					final int returnVal = fc.showDialog(getComponent(), "Ok");
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						final File selected = fc.getSelectedFile();
						String path;
						if (selected.isDirectory()) {
							path = selected.getAbsolutePath() + File.separator + DEFAULT_FILE_NAME;
						} else {
							path = selected.getAbsolutePath();
						}
						txtPath.setText(path);
					}
				}
			});
			btnShowFileChooser.setMnemonic('b');
			final GridBagConstraints gbc_btnShowFileChooser = new GridBagConstraints();
			gbc_btnShowFileChooser.insets = new Insets(0, 0, 5, 0);
			gbc_btnShowFileChooser.gridx = 2;
			gbc_btnShowFileChooser.gridy = 0;
			contentPanel.add(btnShowFileChooser, gbc_btnShowFileChooser);
		}
		{
			final JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				final JButton btnOk = new JButton("OK");

				btnOk.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent arg0) {

						wizardContext.getRemoteManager().startGetRemoteLinkedDisk(txtPath.getText(), wizardContext.getSelectedDiskToLink().getId(),
								new ActionObserver() {

									@Override
									public void onActionFinished(final AbstractBadgerAction action) {
										// i believe this is ugly, is there a way to set the adminInterface in a nicer manner
										// I tend to think that this ActionObserver should not be set, but the RemoteManager must be the ActionObserver for
										// this action, how can we dispose this login dialog from RemoteManager??

										SwingUtilities.invokeLater(new Runnable() {
											@Override
											public void run() {
												dispose();
												try {
													ownerController.openLocalDiskFromRemoteDisk(txtPath.getText());
												} catch (final VFSException e) {
													SwingUtil.handleException(getThis(), e);
												}
											}
										});
									}

									@Override
									public void onActionFailed(final AbstractBadgerAction action, final Exception e) {
										SwingUtil.handleException(getThis(), e);
									}
								});

					}
				});
				btnOk.setMnemonic('o');
				btnOk.setActionCommand("OK");
				buttonPane.add(btnOk);
				getRootPane().setDefaultButton(btnOk);
			}
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
		}

		init();
	}

	private void init() {
		final String home = System.getProperty("user.home");
		txtPath.setText(home + File.separator + DEFAULT_FILE_NAME);
	}

	private void createDisk() {
		try {
			LOGGER.info("Create Disk");

			final DiskConfiguration config = new DiskConfiguration();

			config.setHostFilePath(txtPath.getText());

			ownerController.createDisk(config);
		} catch (final VFSException e) {
			SwingUtil.handleException(this, e);
		}
	}

	protected Component getComponent() {
		return this;
	}

	private GetRemoteLinkedDiskDialog getThis() {
		return this;
	}

}
