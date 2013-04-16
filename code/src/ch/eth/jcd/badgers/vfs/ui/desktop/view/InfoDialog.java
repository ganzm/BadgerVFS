package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.border.EmptyBorder;

public class InfoDialog extends JDialog {

	private static final long serialVersionUID = -8374331741324802496L;
	private final JPanel contentPanel = new JPanel();

	/**
	 * Create the dialog.
	 */
	public InfoDialog(JFrame owner) {
		super(owner, true);
		setTitle("Info");
		setBounds(100, 100, 387, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 0 };
		gbl_contentPanel.rowHeights = new int[] { 64, 0, 0, 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblBadgerVirtualFile = new JLabel("Badger Virtual File System™ 2013");
			lblBadgerVirtualFile.setFont(new Font("Tahoma", Font.BOLD, 16));
			GridBagConstraints gbc_lblBadgerVirtualFile = new GridBagConstraints();
			gbc_lblBadgerVirtualFile.anchor = GridBagConstraints.NORTH;
			gbc_lblBadgerVirtualFile.insets = new Insets(0, 0, 5, 0);
			gbc_lblBadgerVirtualFile.gridx = 0;
			gbc_lblBadgerVirtualFile.gridy = 0;
			contentPanel.add(lblBadgerVirtualFile, gbc_lblBadgerVirtualFile);
		}
		{
			JLabel lblAutor = new JLabel("Autor");
			lblAutor.setFont(new Font("Tahoma", Font.BOLD, 12));
			GridBagConstraints gbc_lblAutor = new GridBagConstraints();
			gbc_lblAutor.insets = new Insets(0, 0, 5, 0);
			gbc_lblAutor.anchor = GridBagConstraints.WEST;
			gbc_lblAutor.gridx = 0;
			gbc_lblAutor.gridy = 1;
			contentPanel.add(lblAutor, gbc_lblAutor);
		}
		{
			JLabel lblFrickThomas = new JLabel("Frick Thomas");
			GridBagConstraints gbc_lblFrickThomas = new GridBagConstraints();
			gbc_lblFrickThomas.insets = new Insets(0, 0, 5, 0);
			gbc_lblFrickThomas.anchor = GridBagConstraints.WEST;
			gbc_lblFrickThomas.gridx = 0;
			gbc_lblFrickThomas.gridy = 2;
			contentPanel.add(lblFrickThomas, gbc_lblFrickThomas);
		}
		{
			JLabel lblGanzMatthias = new JLabel("Ganz Matthias");
			GridBagConstraints gbc_lblGanzMatthias = new GridBagConstraints();
			gbc_lblGanzMatthias.insets = new Insets(0, 0, 5, 0);
			gbc_lblGanzMatthias.anchor = GridBagConstraints.WEST;
			gbc_lblGanzMatthias.gridx = 0;
			gbc_lblGanzMatthias.gridy = 3;
			contentPanel.add(lblGanzMatthias, gbc_lblGanzMatthias);
		}
		{
			JLabel lblRohrPhilipp = new JLabel("Rohr Philipp");
			GridBagConstraints gbc_lblRohrPhilipp = new GridBagConstraints();
			gbc_lblRohrPhilipp.anchor = GridBagConstraints.WEST;
			gbc_lblRohrPhilipp.gridx = 0;
			gbc_lblRohrPhilipp.gridy = 4;
			contentPanel.add(lblRohrPhilipp, gbc_lblRohrPhilipp);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}
}
