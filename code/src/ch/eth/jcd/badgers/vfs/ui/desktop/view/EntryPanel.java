package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class EntryPanel extends JPanel {
	private JTextField textField;
	private JLabel lblIcon;

	/**
	 * Create the panel.
	 */
	public EntryPanel() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		lblIcon = new JLabel("text");
		add(lblIcon);

		textField = new JTextField();
		add(textField);
		textField.setColumns(10);

		textField.setText("asdf");
		textField.setEditable(true);
		textField.setEnabled(true);
	}

	public void setIcon(ImageIcon icon) {
		lblIcon.setIcon(icon);
	}

	public void setText(String displayName) {
		lblIcon.setText(displayName);
	}
}
