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
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.sync.client.ConnectionStateListener;
import ch.eth.jcd.badgers.vfs.sync.client.ConnectionStatus;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.RemoteSynchronisationWizardContext;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.RemoteSynchronisationWizardContext.LoginActionEnum;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public class LoginDialog extends JDialog {

	private static final long serialVersionUID = 6008623672955958103L;
	private final JTextField textFieldUsername;
	private final JPasswordField passwordField;
	private final DesktopController controller;

	private final RemoteSynchronisationWizardContext wizardContext;

	/**
	 * Create the dialog.
	 */
	public LoginDialog(final DesktopController owner, final RemoteSynchronisationWizardContext wizardContext) {
		super((BadgerMainFrame) owner.getView(), true);
		this.wizardContext = wizardContext;
		controller = owner;
		setTitle("Remote Login");
		setBounds(100, 100, 450, 149);
		getContentPane().setLayout(new BorderLayout());

		final JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));

		getContentPane().add(panel, BorderLayout.CENTER);
		final GridBagLayout gblPanel = new GridBagLayout();
		gblPanel.columnWidths = new int[] { 0, 0, 0 };
		gblPanel.rowHeights = new int[] { 0, 0, 0 };
		gblPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gblPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gblPanel);
		{
			final JLabel lblUsername = new JLabel("Username: ");
			final GridBagConstraints gbcLblMaximumSpace = new GridBagConstraints();
			gbcLblMaximumSpace.anchor = GridBagConstraints.EAST;
			gbcLblMaximumSpace.insets = new Insets(0, 0, 2, 2);
			gbcLblMaximumSpace.gridx = 0;
			gbcLblMaximumSpace.gridy = 0;
			panel.add(lblUsername, gbcLblMaximumSpace);
		}
		{
			textFieldUsername = new JTextField();
			textFieldUsername.setHorizontalAlignment(SwingConstants.LEADING);
			final GridBagConstraints gbcTextFieldMaxSpace = new GridBagConstraints();
			gbcTextFieldMaxSpace.insets = new Insets(0, 0, 2, 0);
			gbcTextFieldMaxSpace.fill = GridBagConstraints.HORIZONTAL;
			gbcTextFieldMaxSpace.gridx = 1;
			gbcTextFieldMaxSpace.gridy = 0;
			panel.add(textFieldUsername, gbcTextFieldMaxSpace);
			textFieldUsername.setColumns(10);
		}
		{
			final JLabel lblPassword = new JLabel("Password: ");
			final GridBagConstraints gbcLblMaximumSpace = new GridBagConstraints();
			gbcLblMaximumSpace.anchor = GridBagConstraints.EAST;
			gbcLblMaximumSpace.insets = new Insets(0, 0, 2, 2);
			gbcLblMaximumSpace.gridx = 0;
			gbcLblMaximumSpace.gridy = 1;
			panel.add(lblPassword, gbcLblMaximumSpace);
		}
		{
			passwordField = new JPasswordField();
			passwordField.setHorizontalAlignment(SwingConstants.LEADING);
			final GridBagConstraints gbcTextFieldMaxSpace = new GridBagConstraints();
			gbcTextFieldMaxSpace.insets = new Insets(0, 0, 2, 0);
			gbcTextFieldMaxSpace.fill = GridBagConstraints.HORIZONTAL;
			gbcTextFieldMaxSpace.gridx = 1;
			gbcTextFieldMaxSpace.gridy = 1;
			panel.add(passwordField, gbcTextFieldMaxSpace);
			passwordField.setColumns(10);
		}
		{
			final JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{

				final JButton loginAndSyncButton = new JButton("Login & Sync");
				loginAndSyncButton.addActionListener(getLoginAndSyncActionListener(wizardContext));
				loginAndSyncButton.setMnemonic('l');
				loginAndSyncButton.setActionCommand("LoginSync");
				buttonPane.add(loginAndSyncButton);
				getRootPane().setDefaultButton(loginAndSyncButton);

				final JButton createAndSyncButton = new JButton("Register & Sync");
				createAndSyncButton.addActionListener(getRegisterAndSyncActionListener(wizardContext));
				createAndSyncButton.setMnemonic('r');
				createAndSyncButton.setActionCommand("RegisterSync");
				buttonPane.add(createAndSyncButton);

				final JButton connectButton = new JButton("Connect");
				connectButton.addActionListener(getConnectActionlistener(wizardContext));
				connectButton.setMnemonic('c');
				connectButton.setActionCommand("Connect");
				buttonPane.add(connectButton);
				getRootPane().setDefaultButton(connectButton);

				final JButton loginButton = new JButton("Login");
				loginButton.addActionListener(getLoginActionListener(wizardContext));
				loginButton.setMnemonic('l');
				loginButton.setActionCommand("Login");
				buttonPane.add(loginButton);
				getRootPane().setDefaultButton(loginButton);

				final JButton createButton = new JButton("Register");
				createButton.addActionListener(getRegisterActionListener(wizardContext));
				createButton.setMnemonic('c');
				createButton.setActionCommand("Register");
				buttonPane.add(createButton);
				getRootPane().setDefaultButton(createButton);

				final JButton closeButton = new JButton("Close");
				closeButton.addActionListener(getCloseActionListener());
				closeButton.setMnemonic('c');
				closeButton.setActionCommand("Close");
				buttonPane.add(closeButton);
				getRootPane().setDefaultButton(closeButton);

				if (wizardContext.getLoginActionEnum() == LoginActionEnum.SYNC) {
					loginAndSyncButton.setVisible(true);
					createAndSyncButton.setVisible(true);
					loginButton.setVisible(false);
					connectButton.setVisible(false);
					createButton.setVisible(false);
					closeButton.setVisible(false);

				} else if (wizardContext.getLoginActionEnum() == LoginActionEnum.LOGINREMOTE) {
					loginAndSyncButton.setVisible(false);
					createAndSyncButton.setVisible(false);
					loginButton.setVisible(true);
					connectButton.setVisible(false);
					createButton.setVisible(true);
					closeButton.setVisible(false);

				} else if (wizardContext.getLoginActionEnum() == LoginActionEnum.CONNECT) {
					loginAndSyncButton.setVisible(false);
					createAndSyncButton.setVisible(false);
					loginButton.setVisible(false);
					connectButton.setVisible(true);
					createButton.setVisible(false);
					closeButton.setVisible(false);

				} else {
					loginAndSyncButton.setVisible(false);
					createAndSyncButton.setVisible(false);
					loginButton.setVisible(false);
					connectButton.setVisible(false);
					createButton.setVisible(false);
					closeButton.setVisible(true);
				}
			}
		}

	}

	private ActionListener getCloseActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				dispose();
			}
		};
	}

	private ActionListener getRegisterActionListener(final RemoteSynchronisationWizardContext wizardContext) {
		return new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				wizardContext.setUsername(textFieldUsername.getText());
				wizardContext.setPassword(new String(passwordField.getPassword()));
				wizardContext.getRemoteManager().registerUser(textFieldUsername.getText(), new String(passwordField.getPassword()),
						new ConnectionStateListener() {

							private ConnectionStateListener getConnectionStateListener() {
								return this;
							}

							@Override
							public void connectionStateChanged(final ConnectionStatus status) {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										wizardContext.getRemoteManager().removeConnectionStateListener(getConnectionStateListener());
										dispose();
										controller.openRemoteDiskDialog(wizardContext);

									}
								});

							}

						});

			}
		};
	}

	private ActionListener getLoginActionListener(final RemoteSynchronisationWizardContext wizardContext) {
		return new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				wizardContext.setUsername(textFieldUsername.getText());
				wizardContext.setPassword(new String(passwordField.getPassword()));
				wizardContext.getRemoteManager().startLogin(wizardContext.getUsername(), wizardContext.getPassword(), new ConnectionStateListener() {
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
									wizardContext.getRemoteManager().removeConnectionStateListener(getConnectionStateListener());
									dispose();
									controller.openRemoteDiskDialog(wizardContext);
								}
							});
						}
					}
				});
			}
		};
	}

	private ActionListener getConnectActionlistener(final RemoteSynchronisationWizardContext wizardContext) {
		return new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				wizardContext.setUsername(textFieldUsername.getText());
				wizardContext.setPassword(new String(passwordField.getPassword()));
				wizardContext.getRemoteManager().startLogin(wizardContext.getUsername(), wizardContext.getPassword(), new ConnectionStateListener() {

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
									wizardContext.getRemoteManager().removeConnectionStateListener(getConnectionStateListener());
									dispose();
									try {
										controller.openLinkedDisk(wizardContext.getDiskManager());
									} catch (VFSException e) {
										SwingUtil.handleException(getThis(), e);
									}
								}
							});
						}
					}
				});
			}
		};
	}

	private ActionListener getRegisterAndSyncActionListener(final RemoteSynchronisationWizardContext wizardContext) {
		return new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				wizardContext.getRemoteManager().registerUser(textFieldUsername.getText(), new String(passwordField.getPassword()),
						new ConnectionStateListener() {
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
											wizardContext.getRemoteManager().removeConnectionStateListener(getConnectionStateListener());
											dispose();
											controller.startSyncToServer(getThis().wizardContext);
										}
									});
								}
							}
						});
			}
		};
	}

	private ActionListener getLoginAndSyncActionListener(final RemoteSynchronisationWizardContext wizardContext) {
		return new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				wizardContext.getRemoteManager().startLogin(textFieldUsername.getText(), new String(passwordField.getPassword()),
						new ConnectionStateListener() {
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
											wizardContext.getRemoteManager().removeConnectionStateListener(getConnectionStateListener());
											dispose();
											controller.startSyncToServer(getThis().wizardContext);
										}
									});
								}
							}
						});
			}
		};
	}

	private LoginDialog getThis() {
		return this;
	}
}
