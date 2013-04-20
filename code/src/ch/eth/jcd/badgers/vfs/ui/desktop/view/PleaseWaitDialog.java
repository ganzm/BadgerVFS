package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class PleaseWaitDialog extends JDialog {

	private static final long serialVersionUID = -3154131954881225804L;

	private final JPanel contentPanel = new JPanel();

	/**
	 * Create the dialog.
	 */
	public PleaseWaitDialog() {
		setTitle("processing");
		setModal(true);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
	}

}
