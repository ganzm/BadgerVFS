package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;

public class EntryPanel extends JPanel {
	private static final Logger LOGGER = Logger.getLogger(EntryPanel.class);

	private static final long serialVersionUID = 8212642557692442721L;

	private final JTextField textField;
	private final JLabel lblIcon;

	/**
	 * Create the panel.
	 */
	public EntryPanel(EntryUiModel newEntry) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		lblIcon = new JLabel("text");
		add(lblIcon);

		textField = new JTextField();
		add(textField);

		setText(newEntry.getDisplayName());
		setIcon(newEntry.getIcon());
	}

	public EntryPanel() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		lblIcon = new JLabel("text");
		add(lblIcon);

		textField = new JTextField();
		add(textField);

		// TODO Auto-generated constructor stub
	}

	public void setIcon(ImageIcon icon) {
		lblIcon.setIcon(icon);
	}

	public void setText(String displayName) {
		lblIcon.setText(displayName);
	}

	public void setFocuse() {
		// TODO Auto-generated method stub

	}

}
