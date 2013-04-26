package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;

public class InfoDialog extends JDialog {

	private static final long serialVersionUID = -8374331741324802496L;
	private final JPanel contentPanel = new JPanel();

	/**
	 * Create the dialog.
	 */
	public InfoDialog(final DesktopController owner) {
		super((BadgerMainFrame) owner.getView(), true);
		setTitle("Info");
		setBounds(100, 100, 387, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new GridLayout(0, 1, 0, 0));
		{
			final JLabel lblBadgerVirtualFile = new JLabel("Badger Virtual File System™ 2013");
			lblBadgerVirtualFile.setHorizontalAlignment(SwingConstants.CENTER);
			lblBadgerVirtualFile.setFont(new Font("Tahoma", Font.BOLD, 16));
			contentPanel.add(lblBadgerVirtualFile);
		}
		{
			final JLabel lblAutor = new JLabel("Authors");
			lblAutor.setHorizontalAlignment(SwingConstants.CENTER);
			lblAutor.setFont(new Font("Tahoma", Font.BOLD, 12));
			contentPanel.add(lblAutor);
		}
		{
			final JLabel lblFrickThomas = new JLabel("Frick Thomas");
			lblFrickThomas.setHorizontalAlignment(SwingConstants.CENTER);
			contentPanel.add(lblFrickThomas);
		}
		{
			final JLabel lblGanzMatthias = new JLabel("Ganz Matthias");
			lblGanzMatthias.setHorizontalAlignment(SwingConstants.CENTER);
			contentPanel.add(lblGanzMatthias);
		}
		{
			final JLabel lblRohrPhilipp = new JLabel("Rohr Philipp");
			lblRohrPhilipp.setHorizontalAlignment(SwingConstants.CENTER);
			contentPanel.add(lblRohrPhilipp);
		}
		{
			final JLabel badgerLabel = new JLabel();
			badgerLabel.setHorizontalAlignment(SwingConstants.CENTER);
			badgerLabel.setIcon(new ImageIcon(InfoDialog.class.getResource("/images/badger.png")));
			contentPanel.add(badgerLabel);
		}
		{
			final JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				final JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent arg0) {
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
