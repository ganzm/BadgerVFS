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
import javax.swing.SwingConstants;

import ch.eth.jcd.badgers.vfs.ui.desktop.view.LoginDialog.LoginAction;

public class ServerUrlDialog extends JDialog {

	private static final long serialVersionUID = 6008623672955958103L;
	private JTextField textFieldRemoteServerUrl;
	private final BadgerMainFrame parent;

	/**
	 * Create the dialog.
	 */
	public ServerUrlDialog(JFrame owner, final LoginAction loginAction) {
		super(owner, true);
		parent = (BadgerMainFrame) owner;
		setTitle("Remote Server");
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
				JLabel lblRemoteServerUrl = new JLabel("Remote Server URL: ");
				GridBagConstraints gbcLblMaximumSpace = new GridBagConstraints();
				gbcLblMaximumSpace.anchor = GridBagConstraints.EAST;
				gbcLblMaximumSpace.insets = new Insets(0, 0, 2, 2);
				gbcLblMaximumSpace.gridx = 0;
				gbcLblMaximumSpace.gridy = 0;
				panel.add(lblRemoteServerUrl, gbcLblMaximumSpace);
			}
			{
				textFieldRemoteServerUrl = new JTextField("http://sync_server.bfs.ch");
				textFieldRemoteServerUrl.setHorizontalAlignment(SwingConstants.RIGHT);
				GridBagConstraints gbcTextFieldMaxSpace = new GridBagConstraints();
				gbcTextFieldMaxSpace.insets = new Insets(0, 0, 2, 0);
				gbcTextFieldMaxSpace.fill = GridBagConstraints.HORIZONTAL;
				gbcTextFieldMaxSpace.gridx = 1;
				gbcTextFieldMaxSpace.gridy = 0;
				panel.add(textFieldRemoteServerUrl, gbcTextFieldMaxSpace);
				textFieldRemoteServerUrl.setColumns(10);
			}
			{
				JPanel buttonPane = new JPanel();
				buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
				getContentPane().add(buttonPane, BorderLayout.SOUTH);
				{
					JButton nextButton = new JButton("Next");
					nextButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							dispose();
							parent.getController().openLoginDialog(parent, loginAction);
						}
					});
					nextButton.setMnemonic('n');
					nextButton.setActionCommand("Next");
					buttonPane.add(nextButton);
					getRootPane().setDefaultButton(nextButton);
				}
			}
		}

	}
}
