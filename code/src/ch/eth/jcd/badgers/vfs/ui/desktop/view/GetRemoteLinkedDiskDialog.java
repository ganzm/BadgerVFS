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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.model.Compression;
import ch.eth.jcd.badgers.vfs.core.model.Encryption;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.sync.client.ConnectionStateListener;
import ch.eth.jcd.badgers.vfs.sync.client.ConnectionStatus;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.BadgerViewBase;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.RemoteSynchronisationWizardContext;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public class GetRemoteLinkedDiskDialog extends JDialog implements BadgerViewBase {

	private static final Logger LOGGER = Logger.getLogger(NewDiskCreationDialog.class);
	private static final String DEFAULT_FILE_NAME = "disk.bfs";

	private static final long serialVersionUID = -2652867330270571476L;

	private final JPanel contentPanel = new JPanel();

	private final DesktopController controller;

	private final JTextField txtPath;
	private final JTextField txtMaximumSize;

	private final JComboBox<Encryption> cboEncryption;
	private final JComboBox<Compression> cboCompression;

	/**
	 * Create the dialog.
	 */
	public GetRemoteLinkedDiskDialog(final DesktopController desktopController, final RemoteSynchronisationWizardContext wizardContext) {
		super((BadgerMainFrame) desktopController.getView(), "Create new remote virtual disk", true);
		controller = desktopController;

		setBounds(100, 100, 727, 213);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		final GridBagLayout gblContentPanel = new GridBagLayout();
		gblContentPanel.columnWidths = new int[] { 0, 0, 0, 0 };
		gblContentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gblContentPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gblContentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gblContentPanel);

		final JLabel lblPath = new JLabel("Path");
		final GridBagConstraints gbc_lblPath = new GridBagConstraints();
		gbc_lblPath.anchor = GridBagConstraints.EAST;
		gbc_lblPath.insets = new Insets(0, 0, 5, 5);
		gbc_lblPath.gridx = 0;
		gbc_lblPath.gridy = 0;
		contentPanel.add(lblPath, gbc_lblPath);

		txtPath = new JTextField();
		final GridBagConstraints gbc_txtPath = new GridBagConstraints();
		gbc_txtPath.insets = new Insets(0, 0, 5, 5);
		gbc_txtPath.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPath.gridx = 1;
		gbc_txtPath.gridy = 0;
		contentPanel.add(txtPath, gbc_txtPath);
		txtPath.setColumns(10);

		final JButton btnShowFileChooser = new JButton("Browse");
		btnShowFileChooser.addActionListener(getBrowseActionlistener());
		btnShowFileChooser.setMnemonic('b');
		final GridBagConstraints gbc_btnShowFileChooser = new GridBagConstraints();
		gbc_btnShowFileChooser.insets = new Insets(0, 0, 5, 0);
		gbc_btnShowFileChooser.gridx = 2;
		gbc_btnShowFileChooser.gridy = 0;
		contentPanel.add(btnShowFileChooser, gbc_btnShowFileChooser);

		final JLabel lblMaximumSize = new JLabel("Maximum Size (mb)");
		final GridBagConstraints gbc_lblMaximumSize = new GridBagConstraints();
		gbc_lblMaximumSize.anchor = GridBagConstraints.EAST;
		gbc_lblMaximumSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaximumSize.gridx = 0;
		gbc_lblMaximumSize.gridy = 1;
		contentPanel.add(lblMaximumSize, gbc_lblMaximumSize);

		txtMaximumSize = new JTextField();
		txtMaximumSize.setText("1000");
		final GridBagConstraints gbc_txtMaximumSize = new GridBagConstraints();
		gbc_txtMaximumSize.insets = new Insets(0, 0, 5, 5);
		gbc_txtMaximumSize.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMaximumSize.gridx = 1;
		gbc_txtMaximumSize.gridy = 1;
		contentPanel.add(txtMaximumSize, gbc_txtMaximumSize);
		txtMaximumSize.setColumns(10);

		final JLabel lblEncryption = new JLabel("Encryption");
		final GridBagConstraints gbc_lblEncryption = new GridBagConstraints();
		gbc_lblEncryption.anchor = GridBagConstraints.EAST;
		gbc_lblEncryption.insets = new Insets(0, 0, 5, 5);
		gbc_lblEncryption.gridx = 0;
		gbc_lblEncryption.gridy = 2;
		contentPanel.add(lblEncryption, gbc_lblEncryption);

		cboEncryption = new JComboBox<Encryption>();
		cboEncryption.setModel(new DefaultComboBoxModel<Encryption>(Encryption.values()));
		final GridBagConstraints gbc_cboEncryption = new GridBagConstraints();
		gbc_cboEncryption.insets = new Insets(0, 0, 5, 5);
		gbc_cboEncryption.fill = GridBagConstraints.HORIZONTAL;
		gbc_cboEncryption.gridx = 1;
		gbc_cboEncryption.gridy = 2;
		contentPanel.add(cboEncryption, gbc_cboEncryption);

		final JLabel lblCompression = new JLabel("Compression");
		final GridBagConstraints gbc_lblCompression = new GridBagConstraints();
		gbc_lblCompression.anchor = GridBagConstraints.EAST;
		gbc_lblCompression.insets = new Insets(0, 0, 0, 5);
		gbc_lblCompression.gridx = 0;
		gbc_lblCompression.gridy = 3;
		contentPanel.add(lblCompression, gbc_lblCompression);

		cboCompression = new JComboBox<Compression>();
		cboCompression.setModel(new DefaultComboBoxModel<Compression>(Compression.values()));
		final GridBagConstraints gbc_cboCompression = new GridBagConstraints();
		gbc_cboCompression.insets = new Insets(0, 0, 0, 5);
		gbc_cboCompression.fill = GridBagConstraints.HORIZONTAL;
		gbc_cboCompression.gridx = 1;
		gbc_cboCompression.gridy = 3;
		contentPanel.add(cboCompression, gbc_cboCompression);

		final JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		final JButton btnOk = new JButton("OK");
		btnOk.addActionListener(getOkayActionListener(wizardContext));
		btnOk.setMnemonic('o');
		btnOk.setActionCommand("OK");
		buttonPane.add(btnOk);
		getRootPane().setDefaultButton(btnOk);

		final JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				dispose();
				controller.openRemoteDiskDialog(wizardContext);
			}
		});
		btnCancel.setMnemonic('c');
		btnCancel.setActionCommand("Cancel");
		buttonPane.add(btnCancel);

		init();
	}

	private ActionListener getOkayActionListener(final RemoteSynchronisationWizardContext wizardContext) {
		return new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {

				dispose();
				try {
					wizardContext.getRemoteManager().logout();
					// TODO Warten auf fertiges Logout. Blocking.
					wizardContext.getRemoteManager().dispose();
					createDisk(wizardContext);
					controller.getRemoteManager().startLogin(wizardContext.getUsername(), wizardContext.getPassword(), new ConnectionStateListener() {

						private ConnectionStateListener getConnectionStateListener() {
							return this;
						}

						@Override
						public void connectionStateChanged(final ConnectionStatus status) {

							if (ConnectionStatus.CONNECTED.equals(status)) {
								SwingUtil.handleError(getThis(), "Login failed, wrong username or password");
								wizardContext.getRemoteManager().removeConnectionStateListener(getConnectionStateListener());
							} else if (ConnectionStatus.LOGGED_IN.equals(status)) {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										controller.getRemoteManager().removeConnectionStateListener(getConnectionStateListener());
										dispose();
										try {
											controller.openLinkedDisk(controller.getWorkerController().getDiskManager());
										} catch (VFSException e) {
											SwingUtil.handleException(getThis(), e);
										}
									}
								});
							}
						}
					});
				} catch (final VFSException e) {
					SwingUtil.handleException(getThis(), e);
				}
			}
		};
	}

	private ActionListener getBrowseActionlistener() {
		return new ActionListener() {
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
		};
	}

	private void init() {
		final String home = System.getProperty("user.home");
		txtPath.setText(home + File.separator + DEFAULT_FILE_NAME);
	}

	private GetRemoteLinkedDiskDialog getThis() {
		return this;
	}

	protected Component getComponent() {
		return this;
	}

	@Override
	public void update() {
	}

	private void createDisk(RemoteSynchronisationWizardContext wizardContext) throws VFSException {
		LOGGER.info("Create Disk");

		final DiskConfiguration config = new DiskConfiguration();
		config.setMaximumSize(1024 * 1024 * Long.parseLong(txtMaximumSize.getText()));

		final Compression compression = (Compression) cboCompression.getSelectedItem();
		final Encryption encryption = (Encryption) cboEncryption.getSelectedItem();

		config.setCompressionAlgorithm(compression);
		config.setEncryptionAlgorithm(encryption);

		config.setHostFilePath(txtPath.getText());

		config.setDiskId(wizardContext.getSelectedDiskToLink().getId());
		config.setLinkedHostName(wizardContext.getRemoteHostName());

		controller.createDisk(config);
	}

}
