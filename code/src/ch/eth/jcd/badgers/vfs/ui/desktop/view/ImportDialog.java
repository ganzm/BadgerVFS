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
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSPath;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;

public class ImportDialog extends JDialog {

	private static final long serialVersionUID = 8826924988127176479L;

	private final JPanel contentPanel = new JPanel();
	private JTextField textFieldSource;
	private JTextField textFieldTargetFolder;
	private final DesktopController ownerController;
	private JTextField textFieldTargetName;

	/**
	 * Create the dialog.
	 */
	public ImportDialog(Frame owner, DesktopController ownerController, String targetFolder) {
		this.ownerController = ownerController;

		setTitle("Import from Host File System");
		setBounds(100, 100, 702, 139);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblSource = new JLabel("Source");
			GridBagConstraints gbc_lblSource = new GridBagConstraints();
			gbc_lblSource.anchor = GridBagConstraints.EAST;
			gbc_lblSource.insets = new Insets(0, 0, 5, 5);
			gbc_lblSource.gridx = 0;
			gbc_lblSource.gridy = 0;
			contentPanel.add(lblSource, gbc_lblSource);
		}
		{
			textFieldSource = new JTextField();
			GridBagConstraints gbc_textFieldSource = new GridBagConstraints();
			gbc_textFieldSource.gridwidth = 3;
			gbc_textFieldSource.insets = new Insets(0, 0, 5, 5);
			gbc_textFieldSource.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldSource.gridx = 1;
			gbc_textFieldSource.gridy = 0;
			contentPanel.add(textFieldSource, gbc_textFieldSource);
			textFieldSource.setColumns(10);
		}
		{
			JButton btnBrowseSource = new JButton("Browse");
			btnBrowseSource.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					JFileChooser fc = new JFileChooser();
					fc.setDialogTitle("Choose File/Folder to import");
					fc.setDialogType(JFileChooser.OPEN_DIALOG);
					fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

					int returnVal = fc.showDialog(getComponent(), "Ok");
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File selected = fc.getSelectedFile();

						textFieldSource.setText(selected.getAbsolutePath());
						textFieldTargetName.setText(selected.getName());
					}
				}
			});
			GridBagConstraints gbc_btnBrowseSource = new GridBagConstraints();
			gbc_btnBrowseSource.insets = new Insets(0, 0, 5, 0);
			gbc_btnBrowseSource.gridx = 4;
			gbc_btnBrowseSource.gridy = 0;
			contentPanel.add(btnBrowseSource, gbc_btnBrowseSource);
		}
		{
			JLabel lblTargetFolder = new JLabel("Target");
			GridBagConstraints gbc_lblTargetFolder = new GridBagConstraints();
			gbc_lblTargetFolder.anchor = GridBagConstraints.EAST;
			gbc_lblTargetFolder.insets = new Insets(0, 0, 0, 5);
			gbc_lblTargetFolder.gridx = 0;
			gbc_lblTargetFolder.gridy = 1;
			contentPanel.add(lblTargetFolder, gbc_lblTargetFolder);
		}
		{
			textFieldTargetFolder = new JTextField();
			textFieldTargetFolder.setEditable(false);
			textFieldTargetFolder.setText(targetFolder);
			GridBagConstraints gbc_textFieldTargetFolder = new GridBagConstraints();
			gbc_textFieldTargetFolder.insets = new Insets(0, 0, 0, 5);
			gbc_textFieldTargetFolder.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldTargetFolder.gridx = 1;
			gbc_textFieldTargetFolder.gridy = 1;
			contentPanel.add(textFieldTargetFolder, gbc_textFieldTargetFolder);
			textFieldTargetFolder.setColumns(10);
		}
		{
			JLabel label = new JLabel("/");
			GridBagConstraints gbc_label = new GridBagConstraints();
			gbc_label.anchor = GridBagConstraints.EAST;
			gbc_label.insets = new Insets(0, 0, 0, 5);
			gbc_label.gridx = 2;
			gbc_label.gridy = 1;
			contentPanel.add(label, gbc_label);
		}
		{
			textFieldTargetName = new JTextField();
			GridBagConstraints gbc_textFieldTargetName = new GridBagConstraints();
			gbc_textFieldTargetName.insets = new Insets(0, 0, 0, 5);
			gbc_textFieldTargetName.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldTargetName.gridx = 3;
			gbc_textFieldTargetName.gridy = 1;
			contentPanel.add(textFieldTargetName, gbc_textFieldTargetName);
			textFieldTargetName.setColumns(10);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						startImport();
					}
				});
				okButton.setMnemonic('O');
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setMnemonic('C');
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	protected Component getComponent() {
		return this;
	}

	private void startImport() {
		String sourcePath = textFieldSource.getText();
		String targetFolder = textFieldTargetFolder.getText().trim();
		String targetPath;
		if (VFSPath.FILE_SEPARATOR.equals(targetFolder)) {
			// import into root folder
			targetPath = VFSPath.FILE_SEPARATOR + textFieldTargetName.getText();
		} else {
			targetPath = targetFolder + VFSPath.FILE_SEPARATOR + textFieldTargetName.getText();
		}

		// TODO validate source path
		ownerController.startImportFromHostFs(sourcePath, targetPath);
		dispose();
	}
}
