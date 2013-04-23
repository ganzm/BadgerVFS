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

public class LoginDialog extends JDialog {

	private static final long serialVersionUID = 6008623672955958103L;
	private JTextField textFieldUsername;
	private JPasswordField passwordField;
	private final BadgerMainFrame parent;

	public static enum LoginAction {
		SYNC, LOGIN;
	}

	/**
	 * Create the dialog.
	 */
	public LoginDialog(JFrame owner, LoginAction loginAction) {
		super(owner, true);
		parent = (BadgerMainFrame) owner;
		setTitle("Remote Login");
		setBounds(100, 100, 450, 120);
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel panel = new JPanel();
			getContentPane().add(panel, BorderLayout.CENTER);
			GridBagLayout gblPanel = new GridBagLayout();
			gblPanel.columnWidths = new int[] { 0, 0, 0 };
			gblPanel.rowHeights = new int[] { 0, 0, 0 };
			gblPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gblPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
			panel.setLayout(gblPanel);
			{
				JLabel lblUsername = new JLabel("Username: ");
				GridBagConstraints gbcLblMaximumSpace = new GridBagConstraints();
				gbcLblMaximumSpace.anchor = GridBagConstraints.EAST;
				gbcLblMaximumSpace.insets = new Insets(0, 0, 2, 2);
				gbcLblMaximumSpace.gridx = 0;
				gbcLblMaximumSpace.gridy = 0;
				panel.add(lblUsername, gbcLblMaximumSpace);
			}
			{
				textFieldUsername = new JTextField();
				textFieldUsername.setHorizontalAlignment(SwingConstants.RIGHT);
				GridBagConstraints gbcTextFieldMaxSpace = new GridBagConstraints();
				gbcTextFieldMaxSpace.insets = new Insets(0, 0, 2, 0);
				gbcTextFieldMaxSpace.fill = GridBagConstraints.HORIZONTAL;
				gbcTextFieldMaxSpace.gridx = 1;
				gbcTextFieldMaxSpace.gridy = 0;
				panel.add(textFieldUsername, gbcTextFieldMaxSpace);
				textFieldUsername.setColumns(10);
			}
			{
				JLabel lblPassword = new JLabel("Password: ");
				GridBagConstraints gbcLblMaximumSpace = new GridBagConstraints();
				gbcLblMaximumSpace.anchor = GridBagConstraints.EAST;
				gbcLblMaximumSpace.insets = new Insets(0, 0, 2, 2);
				gbcLblMaximumSpace.gridx = 0;
				gbcLblMaximumSpace.gridy = 1;
				panel.add(lblPassword, gbcLblMaximumSpace);
			}
			{
				passwordField = new JPasswordField();
				passwordField.setHorizontalAlignment(SwingConstants.RIGHT);
				GridBagConstraints gbcTextFieldMaxSpace = new GridBagConstraints();
				gbcTextFieldMaxSpace.insets = new Insets(0, 0, 2, 0);
				gbcTextFieldMaxSpace.fill = GridBagConstraints.HORIZONTAL;
				gbcTextFieldMaxSpace.gridx = 1;
				gbcTextFieldMaxSpace.gridy = 1;
				panel.add(passwordField, gbcTextFieldMaxSpace);
				passwordField.setColumns(10);
			}
			{
				JPanel buttonPane = new JPanel();
				buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
				getContentPane().add(buttonPane, BorderLayout.SOUTH);
				{
					if (loginAction == LoginAction.SYNC) {
						JButton syncButton = new JButton("Sync");
						syncButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg0) {
								dispose();
								parent.getController().openSyncToServerDialog(parent);
							}
						});
						syncButton.setMnemonic('c');
						syncButton.setActionCommand("Create");
						buttonPane.add(syncButton);
						getRootPane().setDefaultButton(syncButton);
					} else if (loginAction == LoginAction.LOGIN) {
						JButton loginButton = new JButton("Login");
						loginButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg0) {
								dispose();
								parent.getController().openRemoteDiskDialog(parent);
							}
						});
						loginButton.setMnemonic('l');
						loginButton.setActionCommand("Login");
						buttonPane.add(loginButton);
						getRootPane().setDefaultButton(loginButton);

						JButton createButton = new JButton("Create");
						createButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg0) {
								dispose();
								parent.getController().openRemoteDiskDialog(parent);
							}
						});
						createButton.setMnemonic('c');
						createButton.setActionCommand("Create");
						buttonPane.add(createButton);
						getRootPane().setDefaultButton(createButton);
					} else {
						JButton closeButton = new JButton("Close");
						closeButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg0) {
								dispose();
							}
						});
						closeButton.setMnemonic('c');
						closeButton.setActionCommand("Close");
						buttonPane.add(closeButton);
						getRootPane().setDefaultButton(closeButton);
					}
				}
			}
		}

	}
}
