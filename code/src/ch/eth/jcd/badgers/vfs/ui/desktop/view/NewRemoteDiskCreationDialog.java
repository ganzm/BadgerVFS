package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.RemoteSynchronisationWizardContext;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public class NewRemoteDiskCreationDialog extends JDialog {

	private static final Logger LOGGER = Logger.getLogger(NewRemoteDiskCreationDialog.class);
	private static final String DEFAULT_FILE_NAME = "disk.bfs";

	private static final long serialVersionUID = -2652867330270571476L;

	private final DesktopController controller;

	private final JPanel contentPanel = new JPanel();
	private final JTextField txtFilename;

	/**
	 * Create the dialog.
	 */
	public NewRemoteDiskCreationDialog(final DesktopController ownerController, final RemoteSynchronisationWizardContext wizardContext) {
		super((BadgerMainFrame) ownerController.getView(), "Set filepath for remote disk", true);
		this.controller = ownerController;

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
			final JLabel lblFilename = new JLabel("Filename");
			final GridBagConstraints gbc_lblPath = new GridBagConstraints();
			gbc_lblPath.anchor = GridBagConstraints.EAST;
			gbc_lblPath.insets = new Insets(0, 0, 5, 5);
			gbc_lblPath.gridx = 0;
			gbc_lblPath.gridy = 0;
			contentPanel.add(lblFilename, gbc_lblPath);
		}
		{
			txtFilename = new JTextField();
			final GridBagConstraints gbc_txtPath = new GridBagConstraints();
			gbc_txtPath.insets = new Insets(0, 0, 5, 5);
			gbc_txtPath.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtPath.gridx = 1;
			gbc_txtPath.gridy = 0;
			contentPanel.add(txtFilename, gbc_txtPath);
			txtFilename.setColumns(10);
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

						wizardContext.getRemoteManager().startCreateNewDisk(txtFilename.getText(), new ActionObserver() {

							@Override
							public void onActionFinished(final AbstractBadgerAction action) {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										dispose();
										controller.openRemoteDiskDialog(wizardContext);
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
		txtFilename.setText(DEFAULT_FILE_NAME);
	}

	protected Component getComponent() {
		return this;
	}

	private NewRemoteDiskCreationDialog getThis() {
		return this;
	}

}
