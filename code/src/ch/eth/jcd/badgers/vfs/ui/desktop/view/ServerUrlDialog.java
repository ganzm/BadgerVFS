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
import javax.swing.border.EmptyBorder;

import ch.eth.jcd.badgers.vfs.ui.desktop.model.RemoteSynchronisationWizardContext;

public class ServerUrlDialog extends JDialog {

	private static final long serialVersionUID = 6008623672955958103L;
	private JTextField textFieldRemoteServerUrl;
	private final BadgerMainFrame parent;
	private final RemoteSynchronisationWizardContext wizardContext;

	/**
	 * Create the dialog.
	 */
	public ServerUrlDialog(JFrame owner, final RemoteSynchronisationWizardContext wizardContext) {
		super(owner, true);
		this.wizardContext = wizardContext;
		parent = (BadgerMainFrame) owner;
		setTitle("Remote Server");
		setBounds(100, 100, 450, 120);
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel panel = new JPanel();
			panel.setBorder(new EmptyBorder(2, 2, 2, 2));
			getContentPane().add(panel, BorderLayout.CENTER);
			GridBagLayout gblPanel = new GridBagLayout();
			gblPanel.columnWidths = new int[] { 0, 0, 0 };
			gblPanel.rowHeights = new int[] { 0, 0, 0 };
			gblPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gblPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
			panel.setLayout(gblPanel);
			{
				JLabel lblRemoteServerUrl = new JLabel("Remote Server Hostname: ");
				GridBagConstraints gbcLblMaximumSpace = new GridBagConstraints();
				gbcLblMaximumSpace.anchor = GridBagConstraints.EAST;
				gbcLblMaximumSpace.insets = new Insets(0, 0, 2, 2);
				gbcLblMaximumSpace.gridx = 0;
				gbcLblMaximumSpace.gridy = 0;
				panel.add(lblRemoteServerUrl, gbcLblMaximumSpace);
			}
			{
				textFieldRemoteServerUrl = new JTextField("localhost");
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
							parent.getController().openLoginDialog(parent, wizardContext);
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
