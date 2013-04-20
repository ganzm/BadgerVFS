package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import ch.eth.jcd.badgers.vfs.ui.desktop.action.BadgerAction;

public class PleaseWaitDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = -3154131954881225804L;

	private final JPanel contentPanel = new JPanel();

	private BadgerAction currentAction;
	private JLabel lblHeader;
	private JProgressBar progressBar;

	/**
	 * Create the dialog.
	 */
	public PleaseWaitDialog() {
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setTitle("processing");
		setModal(true);
		setBounds(100, 100, 330, 100);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 314, 0 };
		gbl_contentPanel.rowHeights = new int[] { 50, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);

		lblHeader = new JLabel("New label");
		lblHeader.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblHeader = new GridBagConstraints();
		gbc_lblHeader.insets = new Insets(0, 0, 5, 0);
		gbc_lblHeader.fill = GridBagConstraints.BOTH;
		gbc_lblHeader.gridx = 0;
		gbc_lblHeader.gridy = 0;
		contentPanel.add(lblHeader, gbc_lblHeader);

		progressBar = new JProgressBar();
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 1;
		contentPanel.add(progressBar, gbc_progressBar);
	}

	public void setCurrentAction(BadgerAction action) {
		currentAction = action;
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			lblHeader.setText(currentAction.getActionName());
			progressBar.setVisible(currentAction.isProgressIndicationSupported());

			swingTimer.start();
		} else {
			swingTimer.stop();
		}

		super.setVisible(b);
	}

	private Timer swingTimer = new Timer(500, this);

	@Override
	public void actionPerformed(ActionEvent arg0) {
		lblHeader.setText(currentAction.getActionName());

		progressBar.setMaximum(currentAction.getMaxProgress());
		progressBar.setValue(currentAction.getCurrentProgress());
	}

}
