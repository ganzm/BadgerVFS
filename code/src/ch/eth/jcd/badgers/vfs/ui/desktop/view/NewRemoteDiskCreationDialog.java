package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.model.Compression;
import ch.eth.jcd.badgers.vfs.core.model.Encryption;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.BadgerViewBase;

public class NewRemoteDiskCreationDialog extends JDialog implements BadgerViewBase {

	private static final Logger LOGGER = Logger.getLogger(NewDiskCreationDialog.class);
	private static final String DEFAULT_FILE_NAME = "disk.bfs";

	private static final long serialVersionUID = -2652867330270571476L;

	private final JPanel contentPanel = new JPanel();
	private final JTextField txtPath;
	private final JTextField txtMaximumSize;

	private final JComboBox<Encryption> cboEncryption;
	private final JComboBox<Compression> cboCompression;
	private final BadgerMainFrame parent;

	/**
	 * Create the dialog.
	 */
	public NewRemoteDiskCreationDialog(final Frame owner) {
		super(owner, "Create new remote virtual disk", true);
		parent = (BadgerMainFrame) owner;

		setBounds(100, 100, 727, 213);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		final GridBagLayout gblContentPanel = new GridBagLayout();
		gblContentPanel.columnWidths = new int[] { 0, 0, 0, 0 };
		gblContentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gblContentPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gblContentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gblContentPanel);
		{
			final JLabel lblFilename = new JLabel("Filename");
			final GridBagConstraints gbc_lblPath = new GridBagConstraints();
			gbc_lblPath.anchor = GridBagConstraints.EAST;
			gbc_lblPath.insets = new Insets(0, 0, 5, 5);
			gbc_lblPath.gridx = 0;
			gbc_lblPath.gridy = 0;
			contentPanel.add(lblFilename, gbc_lblPath);
		}
		{
			txtPath = new JTextField();
			final GridBagConstraints gbc_txtPath = new GridBagConstraints();
			gbc_txtPath.insets = new Insets(0, 0, 5, 5);
			gbc_txtPath.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtPath.gridx = 1;
			gbc_txtPath.gridy = 0;
			contentPanel.add(txtPath, gbc_txtPath);
			txtPath.setColumns(10);
		}
		{
			final JLabel lblMaximumSize = new JLabel("Maximum Size (mb)");
			final GridBagConstraints gbc_lblMaximumSize = new GridBagConstraints();
			gbc_lblMaximumSize.anchor = GridBagConstraints.EAST;
			gbc_lblMaximumSize.insets = new Insets(0, 0, 5, 5);
			gbc_lblMaximumSize.gridx = 0;
			gbc_lblMaximumSize.gridy = 1;
			contentPanel.add(lblMaximumSize, gbc_lblMaximumSize);
		}
		{
			txtMaximumSize = new JTextField();
			txtMaximumSize.setText("1000");
			final GridBagConstraints gbc_txtMaximumSize = new GridBagConstraints();
			gbc_txtMaximumSize.insets = new Insets(0, 0, 5, 5);
			gbc_txtMaximumSize.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtMaximumSize.gridx = 1;
			gbc_txtMaximumSize.gridy = 1;
			contentPanel.add(txtMaximumSize, gbc_txtMaximumSize);
			txtMaximumSize.setColumns(10);
		}
		{
			final JLabel lblEncryption = new JLabel("Encryption");
			final GridBagConstraints gbc_lblEncryption = new GridBagConstraints();
			gbc_lblEncryption.anchor = GridBagConstraints.EAST;
			gbc_lblEncryption.insets = new Insets(0, 0, 5, 5);
			gbc_lblEncryption.gridx = 0;
			gbc_lblEncryption.gridy = 2;
			contentPanel.add(lblEncryption, gbc_lblEncryption);
		}
		{
			cboEncryption = new JComboBox<Encryption>();
			cboEncryption.setModel(new DefaultComboBoxModel<Encryption>(Encryption.values()));
			final GridBagConstraints gbc_cboEncryption = new GridBagConstraints();
			gbc_cboEncryption.insets = new Insets(0, 0, 5, 5);
			gbc_cboEncryption.fill = GridBagConstraints.HORIZONTAL;
			gbc_cboEncryption.gridx = 1;
			gbc_cboEncryption.gridy = 2;
			contentPanel.add(cboEncryption, gbc_cboEncryption);
		}
		{
			final JLabel lblCompression = new JLabel("Compression");
			final GridBagConstraints gbc_lblCompression = new GridBagConstraints();
			gbc_lblCompression.anchor = GridBagConstraints.EAST;
			gbc_lblCompression.insets = new Insets(0, 0, 0, 5);
			gbc_lblCompression.gridx = 0;
			gbc_lblCompression.gridy = 3;
			contentPanel.add(lblCompression, gbc_lblCompression);
		}
		{
			cboCompression = new JComboBox<Compression>();
			cboCompression.setModel(new DefaultComboBoxModel<Compression>(Compression.values()));
			final GridBagConstraints gbc_cboCompression = new GridBagConstraints();
			gbc_cboCompression.insets = new Insets(0, 0, 0, 5);
			gbc_cboCompression.fill = GridBagConstraints.HORIZONTAL;
			gbc_cboCompression.gridx = 1;
			gbc_cboCompression.gridy = 3;
			contentPanel.add(cboCompression, gbc_cboCompression);
		}
		{
			final JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				final JButton btnOk = new JButton("OK");
				btnOk.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						// createDisk();
						dispose();
						parent.getController().openRemoteDiskDialog(parent);
					}
				});
				btnOk.setMnemonic('o');
				btnOk.setActionCommand("OK");
				buttonPane.add(btnOk);
				getRootPane().setDefaultButton(btnOk);
			}
			{
				final JButton btnCancel = new JButton("Cancel");
				btnCancel.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent arg0) {
						dispose();
						parent.getController().openRemoteDiskDialog(parent);
					}
				});
				btnCancel.setMnemonic('c');
				btnCancel.setActionCommand("Cancel");
				buttonPane.add(btnCancel);
			}
		}

		init();
	}

	private void init() {
		txtPath.setText(DEFAULT_FILE_NAME);
	}

	protected Component getComponent() {
		return this;
	}

	@Override
	public void update() {
	}
}
