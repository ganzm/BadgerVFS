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
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.RemoteSynchronisationWizardContext;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.RemoteSynchronisationWizardContext.LoginActionEnum;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public class LoginDialog extends JDialog {

	private static final long serialVersionUID = 6008623672955958103L;
	private JTextField textFieldUsername;
	private JPasswordField passwordField;
	private final BadgerMainFrame parent;

	private final RemoteSynchronisationWizardContext wizardContext;

	/**
	 * Create the dialog.
	 */
	public LoginDialog(final JFrame owner, final RemoteSynchronisationWizardContext wizardContext) {
		super(owner, true);
		this.wizardContext = wizardContext;
		parent = (BadgerMainFrame) owner;
		setTitle("Remote Login");
		setBounds(100, 100, 450, 120);
		getContentPane().setLayout(new BorderLayout());
		{
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

					final JButton syncButton = new JButton("Sync");
					syncButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent arg0) {
							dispose();
							parent.getController().startSyncToServer(getThis().wizardContext);
						}
					});
					syncButton.setMnemonic('c');
					syncButton.setActionCommand("Create");
					buttonPane.add(syncButton);
					getRootPane().setDefaultButton(syncButton);

					final JButton loginButton = new JButton("Login");
					loginButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent arg0) {
							wizardContext.getRemoteManager().startLogin(textFieldUsername.getText(), passwordField.getText(), new ActionObserver() {

								@Override
								public void onActionFinished(final AbstractBadgerAction action) {

									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											dispose();
											parent.getController().openRemoteDiskDialog(parent, wizardContext);

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
					loginButton.setMnemonic('l');
					loginButton.setActionCommand("Login");
					buttonPane.add(loginButton);
					getRootPane().setDefaultButton(loginButton);

					final JButton createButton = new JButton("Create");
					createButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent arg0) {
							dispose();
							parent.getController().openRemoteDiskDialog(parent, wizardContext);
						}
					});
					createButton.setMnemonic('c');
					createButton.setActionCommand("Create");
					buttonPane.add(createButton);
					getRootPane().setDefaultButton(createButton);

					final JButton closeButton = new JButton("Close");
					closeButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent arg0) {
							dispose();
						}
					});
					closeButton.setMnemonic('c');
					closeButton.setActionCommand("Close");
					buttonPane.add(closeButton);
					getRootPane().setDefaultButton(closeButton);

					if (wizardContext.getLoginActionEnum() == LoginActionEnum.SYNC) {
						syncButton.setVisible(true);
						loginButton.setVisible(false);
						createButton.setVisible(false);
						closeButton.setVisible(false);

					} else if (wizardContext.getLoginActionEnum() == LoginActionEnum.LOGIN) {
						syncButton.setVisible(false);
						loginButton.setVisible(true);
						createButton.setVisible(true);
						closeButton.setVisible(false);
					} else {
						syncButton.setVisible(false);
						loginButton.setVisible(false);
						createButton.setVisible(false);
						closeButton.setVisible(true);
					}
				}
			}
		}
	}

	private LoginDialog getThis() {
		return this;
	}
}
