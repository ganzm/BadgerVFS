package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.config.DiskConfiguration;
import ch.eth.jcd.badgers.vfs.core.model.Compression;
import ch.eth.jcd.badgers.vfs.core.model.Encryption;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.BadgerViewBase;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;

public class NewDiskCreationDialog extends JDialog implements BadgerViewBase {

	private static final Logger LOGGER = Logger.getLogger(NewDiskCreationDialog.class);

	private static final long serialVersionUID = -2652867330270571476L;

	private final DesktopController ownerController;

	private final JPanel contentPanel = new JPanel();
	private JTextField txtPath;
	private JTextField txtMaximumSize;

	private JComboBox<Encryption> cboEncryption;
	private JComboBox<Compression> cboCompression;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			NewDiskCreationDialog dialog = new NewDiskCreationDialog(null, null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public NewDiskCreationDialog(Frame owner, DesktopController ownerController) {
		super(owner, "Create new virtual disk", true);
		this.ownerController = ownerController;

		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblPath = new JLabel("Path");
			GridBagConstraints gbc_lblPath = new GridBagConstraints();
			gbc_lblPath.anchor = GridBagConstraints.EAST;
			gbc_lblPath.insets = new Insets(0, 0, 5, 5);
			gbc_lblPath.gridx = 0;
			gbc_lblPath.gridy = 0;
			contentPanel.add(lblPath, gbc_lblPath);
		}
		{
			txtPath = new JTextField();
			GridBagConstraints gbc_txtPath = new GridBagConstraints();
			gbc_txtPath.insets = new Insets(0, 0, 5, 5);
			gbc_txtPath.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtPath.gridx = 1;
			gbc_txtPath.gridy = 0;
			contentPanel.add(txtPath, gbc_txtPath);
			txtPath.setColumns(10);
		}
		{
			JButton btnShowFileChooser = new JButton("Browse");
			btnShowFileChooser.setMnemonic('b');
			GridBagConstraints gbc_btnShowFileChooser = new GridBagConstraints();
			gbc_btnShowFileChooser.insets = new Insets(0, 0, 5, 0);
			gbc_btnShowFileChooser.gridx = 2;
			gbc_btnShowFileChooser.gridy = 0;
			contentPanel.add(btnShowFileChooser, gbc_btnShowFileChooser);
		}
		{
			JLabel lblMaximumSize = new JLabel("Maximum Size");
			GridBagConstraints gbc_lblMaximumSize = new GridBagConstraints();
			gbc_lblMaximumSize.anchor = GridBagConstraints.EAST;
			gbc_lblMaximumSize.insets = new Insets(0, 0, 5, 5);
			gbc_lblMaximumSize.gridx = 0;
			gbc_lblMaximumSize.gridy = 1;
			contentPanel.add(lblMaximumSize, gbc_lblMaximumSize);
		}
		{
			txtMaximumSize = new JTextField();
			GridBagConstraints gbc_txtMaximumSize = new GridBagConstraints();
			gbc_txtMaximumSize.insets = new Insets(0, 0, 5, 5);
			gbc_txtMaximumSize.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtMaximumSize.gridx = 1;
			gbc_txtMaximumSize.gridy = 1;
			contentPanel.add(txtMaximumSize, gbc_txtMaximumSize);
			txtMaximumSize.setColumns(10);
		}
		{
			JLabel lblEncryption = new JLabel("Encryption");
			GridBagConstraints gbc_lblEncryption = new GridBagConstraints();
			gbc_lblEncryption.anchor = GridBagConstraints.EAST;
			gbc_lblEncryption.insets = new Insets(0, 0, 5, 5);
			gbc_lblEncryption.gridx = 0;
			gbc_lblEncryption.gridy = 2;
			contentPanel.add(lblEncryption, gbc_lblEncryption);
		}
		{
			cboEncryption = new JComboBox<Encryption>();
			cboEncryption.setModel(new DefaultComboBoxModel<Encryption>(Encryption.values()));
			GridBagConstraints gbc_cboEncryption = new GridBagConstraints();
			gbc_cboEncryption.insets = new Insets(0, 0, 5, 5);
			gbc_cboEncryption.fill = GridBagConstraints.HORIZONTAL;
			gbc_cboEncryption.gridx = 1;
			gbc_cboEncryption.gridy = 2;
			contentPanel.add(cboEncryption, gbc_cboEncryption);
		}
		{
			JLabel lblCompression = new JLabel("Compression");
			GridBagConstraints gbc_lblCompression = new GridBagConstraints();
			gbc_lblCompression.anchor = GridBagConstraints.EAST;
			gbc_lblCompression.insets = new Insets(0, 0, 0, 5);
			gbc_lblCompression.gridx = 0;
			gbc_lblCompression.gridy = 3;
			contentPanel.add(lblCompression, gbc_lblCompression);
		}
		{
			cboCompression = new JComboBox<Compression>();
			cboCompression.setModel(new DefaultComboBoxModel<Compression>(Compression.values()));
			GridBagConstraints gbc_cboCompression = new GridBagConstraints();
			gbc_cboCompression.insets = new Insets(0, 0, 0, 5);
			gbc_cboCompression.fill = GridBagConstraints.HORIZONTAL;
			gbc_cboCompression.gridx = 1;
			gbc_cboCompression.gridy = 3;
			contentPanel.add(cboCompression, gbc_cboCompression);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton btnOk = new JButton("OK");
				btnOk.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						createDisk();
						dispose();

					}

				});
				btnOk.setMnemonic('o');
				btnOk.setActionCommand("OK");
				buttonPane.add(btnOk);
				getRootPane().setDefaultButton(btnOk);
			}
			{
				JButton btnCancel = new JButton("Cancel");
				btnCancel.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						dispose();
					}
				});
				btnCancel.setMnemonic('c');
				btnCancel.setActionCommand("Cancel");
				buttonPane.add(btnCancel);
			}
		}
	}

	private void createDisk() {
		try {
			DiskConfiguration config = new DiskConfiguration();
			config.setMaximumSize(1024 * 1024 * Long.parseLong(txtMaximumSize.getText()));

			Compression compression = (Compression) cboCompression.getSelectedItem();
			Encryption encryption = (Encryption) cboEncryption.getSelectedItem();

			config.setCompressionAlgorithm(compression);
			config.setEncryptionAlgorithm(encryption);

			config.setHostFilePath(txtPath.getText());

			ownerController.createDisk(config);
		} catch (VFSException e) {
			LOGGER.error("", e);
			JOptionPane.showMessageDialog(this, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void update() {
	}
}
