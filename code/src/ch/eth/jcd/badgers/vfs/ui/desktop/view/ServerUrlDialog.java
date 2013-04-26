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
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.sync.client.ConnectionStateListener;
import ch.eth.jcd.badgers.vfs.sync.client.ConnectionStatus;
import ch.eth.jcd.badgers.vfs.sync.client.RemoteManager;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.RemoteSynchronisationWizardContext;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public class ServerUrlDialog extends JDialog {

	private static final Logger LOGGER = Logger.getLogger(ServerUrlDialog.class);
	private static final long serialVersionUID = 6008623672955958103L;
	private JTextField textFieldRemoteServerUrl;
	private final BadgerMainFrame parent;
	private final RemoteSynchronisationWizardContext wizardContext;

	/**
	 * Create the dialog.
	 */
	public ServerUrlDialog(final JFrame owner, final RemoteSynchronisationWizardContext wizardContext) {
		super(owner, true);
		this.wizardContext = wizardContext;
		parent = (BadgerMainFrame) owner;
		setTitle("Remote Server");
		setBounds(100, 100, 450, 80);
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
				final JLabel lblRemoteServerUrl = new JLabel("Remote Server Hostname: ");
				final GridBagConstraints gbcLblMaximumSpace = new GridBagConstraints();
				gbcLblMaximumSpace.anchor = GridBagConstraints.EAST;
				gbcLblMaximumSpace.insets = new Insets(0, 0, 2, 2);
				gbcLblMaximumSpace.gridx = 0;
				gbcLblMaximumSpace.gridy = 0;
				panel.add(lblRemoteServerUrl, gbcLblMaximumSpace);
			}
			{
				textFieldRemoteServerUrl = new JTextField("localhost");
				textFieldRemoteServerUrl.setHorizontalAlignment(SwingConstants.LEADING);
				final GridBagConstraints gbcTextFieldMaxSpace = new GridBagConstraints();
				gbcTextFieldMaxSpace.insets = new Insets(0, 0, 2, 0);
				gbcTextFieldMaxSpace.fill = GridBagConstraints.HORIZONTAL;
				gbcTextFieldMaxSpace.gridx = 1;
				gbcTextFieldMaxSpace.gridy = 0;
				panel.add(textFieldRemoteServerUrl, gbcTextFieldMaxSpace);
				textFieldRemoteServerUrl.setColumns(10);
			}
			{
				final JPanel buttonPane = new JPanel();
				buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
				getContentPane().add(buttonPane, BorderLayout.SOUTH);
				{
					final JButton nextButton = new JButton("Next");
					nextButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent arg0) {
							wizardContext.setRemoteHostName(textFieldRemoteServerUrl.getText());
							final RemoteManager remoteManager = new RemoteManager(wizardContext.getRemoteHostName());
							remoteManager.addConnectionStateListener(new ConnectionStateListener() {

								@Override
								public void connectionStateChanged(final ConnectionStatus status) {
									if (ConnectionStatus.CONNECTED == status) {
										wizardContext.setRemoteManager(remoteManager);
										// onActionFinished wird vom WorkerController Thread aufgerufen
										// wenn "dispse()" und "openLoginDialog" auch im "WorkerControllerThread" ausgeführt werden
										// bleibt alles andere im WorkerController Thread stehen, bis der neu geöffnete "openRemoteDiskDialog" geschlossen wird
										// deswegen muss dieser code im swing-thread gestartet werden, mit "Swingutilities.invokeLater"
										// rop: ich habe es auch gleich im LoginDialog so eingebaut
										SwingUtilities.invokeLater(new Runnable() {

											@Override
											public void run() {
												dispose();
												parent.getController().openLoginDialog(parent, wizardContext);
												LOGGER.debug("Connected to Server");

											}
										});

									}
									if (ConnectionStatus.DISCONNECTED == status) {
										// TODO implement correct disconnect.
										SwingUtil.showWarning(getThis(), "Cannot connect to the Server");
										LOGGER.debug("Cannot connect to the Server");
									}
								}
							});
							remoteManager.start();
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

	private ServerUrlDialog getThis() {
		return this;
	}
}
