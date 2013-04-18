package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
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

import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;
import ch.eth.jcd.badgers.vfs.util.PathUtil;

public class ImportDialog extends JDialog {

	private static final long serialVersionUID = 8826924988127176479L;

	private final JPanel contentPanel = new JPanel();
	private final JTextField textFieldSource;
	private final JTextField textFieldTargetFolder;
	private final DesktopController ownerController;
	private final JTextField textFieldTargetName;

	/**
	 * Create the dialog.
	 */
	public ImportDialog(DesktopController ownerController, String targetFolder) {
		this.ownerController = ownerController;

		setTitle("Import from Host File System");
		setBounds(100, 100, 702, 139);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gblContentPanel = new GridBagLayout();
		gblContentPanel.columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
		gblContentPanel.rowHeights = new int[] { 0, 0, 0 };
		gblContentPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gblContentPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gblContentPanel);
		{
			JLabel lblSource = new JLabel("Source");
			GridBagConstraints gbcLblSource = new GridBagConstraints();
			gbcLblSource.anchor = GridBagConstraints.EAST;
			gbcLblSource.insets = new Insets(0, 0, 5, 5);
			gbcLblSource.gridx = 0;
			gbcLblSource.gridy = 0;
			contentPanel.add(lblSource, gbcLblSource);
		}
		{
			textFieldSource = new JTextField();
			GridBagConstraints gbcTextFieldSource = new GridBagConstraints();
			gbcTextFieldSource.gridwidth = 3;
			gbcTextFieldSource.insets = new Insets(0, 0, 5, 5);
			gbcTextFieldSource.fill = GridBagConstraints.HORIZONTAL;
			gbcTextFieldSource.gridx = 1;
			gbcTextFieldSource.gridy = 0;
			contentPanel.add(textFieldSource, gbcTextFieldSource);
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
			GridBagConstraints gbcBtnBrowseSource = new GridBagConstraints();
			gbcBtnBrowseSource.insets = new Insets(0, 0, 5, 0);
			gbcBtnBrowseSource.gridx = 4;
			gbcBtnBrowseSource.gridy = 0;
			contentPanel.add(btnBrowseSource, gbcBtnBrowseSource);
		}
		{
			JLabel lblTargetFolder = new JLabel("Target");
			GridBagConstraints gbclblTargetFolder = new GridBagConstraints();
			gbclblTargetFolder.anchor = GridBagConstraints.EAST;
			gbclblTargetFolder.insets = new Insets(0, 0, 0, 5);
			gbclblTargetFolder.gridx = 0;
			gbclblTargetFolder.gridy = 1;
			contentPanel.add(lblTargetFolder, gbclblTargetFolder);
		}
		{
			textFieldTargetFolder = new JTextField();
			textFieldTargetFolder.setEditable(false);
			textFieldTargetFolder.setText(targetFolder);
			GridBagConstraints gbctextFieldTargetFolder = new GridBagConstraints();
			gbctextFieldTargetFolder.insets = new Insets(0, 0, 0, 5);
			gbctextFieldTargetFolder.fill = GridBagConstraints.HORIZONTAL;
			gbctextFieldTargetFolder.gridx = 1;
			gbctextFieldTargetFolder.gridy = 1;
			contentPanel.add(textFieldTargetFolder, gbctextFieldTargetFolder);
			textFieldTargetFolder.setColumns(10);
		}
		{
			JLabel label = new JLabel("/");
			GridBagConstraints gbclabel = new GridBagConstraints();
			gbclabel.anchor = GridBagConstraints.EAST;
			gbclabel.insets = new Insets(0, 0, 0, 5);
			gbclabel.gridx = 2;
			gbclabel.gridy = 1;
			contentPanel.add(label, gbclabel);
		}
		{
			textFieldTargetName = new JTextField();
			GridBagConstraints gbctextFieldTargetName = new GridBagConstraints();
			gbctextFieldTargetName.insets = new Insets(0, 0, 0, 5);
			gbctextFieldTargetName.fill = GridBagConstraints.HORIZONTAL;
			gbctextFieldTargetName.gridx = 3;
			gbctextFieldTargetName.gridy = 1;
			contentPanel.add(textFieldTargetName, gbctextFieldTargetName);
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
		String targetPath = PathUtil.concatPathAndFileName(targetFolder, textFieldTargetName.getText());

		// TODO validate source path
		ownerController.startImportFromHostFs(sourcePath, targetPath);
		dispose();
	}

}
