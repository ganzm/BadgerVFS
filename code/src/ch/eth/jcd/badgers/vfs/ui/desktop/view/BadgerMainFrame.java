package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.Initialisation;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.BadgerViewBase;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public class BadgerMainFrame extends JFrame implements BadgerViewBase {

	private static final long serialVersionUID = -8776317677851635247L;
	private static final Logger LOGGER = Logger.getLogger(BadgerMainFrame.class);

	public static final String BROWSE_PANEL_NAME = "browsepanel";
	public static final String SEARCH_PANEL_NAME = "searchpanel";

	private final JPanel contentPane;

	private final DesktopController desktopController = new DesktopController(this);

	private final JTextField textFieldCurrentPath;

	private final SearchPanel panelSearch;
	/**
	 * State Variable determines whether search or browse gui is shown
	 */
	private boolean searching = false;

	private final BadgerMenuBar menuBar;

	private final BadgerTable table;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		Initialisation.initLog4J(args);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {

				try {
					for (final LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
						if ("Nimbus".equals(info.getName())) {
							UIManager.setLookAndFeel(info.getClassName());
							break;
						}
					}
				} catch (final Exception e) {
					// If Nimbus is not available, you can set the GUI to another look and feel.
				}
				final BadgerMainFrame frame = new BadgerMainFrame();
				frame.update();
				frame.setVisible(true);
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public BadgerMainFrame() {
		setTitle("BadgerFS Client");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent ev) {
				beforeWindowClosing();
			}
		});

		setBounds(100, 100, 900, 631);

		menuBar = new BadgerMenuBar(this);
		setJMenuBar(menuBar);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		contentPane.setLayout(new CardLayout());

		final JPanel panelBrowsing = new JPanel();
		contentPane.add(panelBrowsing, BROWSE_PANEL_NAME);
		panelBrowsing.setLayout(new BorderLayout(0, 0));

		final JPanel panelPathLocator = new JPanel();
		panelBrowsing.add(panelPathLocator, BorderLayout.NORTH);
		final GridBagLayout gbl_panelPathLocator = new GridBagLayout();
		gbl_panelPathLocator.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelPathLocator.rowHeights = new int[] { 0, 0 };
		gbl_panelPathLocator.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelPathLocator.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelPathLocator.setLayout(gbl_panelPathLocator);

		final JLabel lblPath = new JLabel("Path");
		final GridBagConstraints gbc_lblPath = new GridBagConstraints();
		gbc_lblPath.insets = new Insets(0, 0, 0, 5);
		gbc_lblPath.anchor = GridBagConstraints.EAST;
		gbc_lblPath.gridx = 0;
		gbc_lblPath.gridy = 0;
		panelPathLocator.add(lblPath, gbc_lblPath);

		textFieldCurrentPath = new JTextField();
		textFieldCurrentPath.setFont(new Font("Tahoma", Font.BOLD, 12));
		textFieldCurrentPath.setEditable(false);
		final GridBagConstraints gbc_textFieldCurrentPath = new GridBagConstraints();
		gbc_textFieldCurrentPath.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldCurrentPath.gridx = 1;
		gbc_textFieldCurrentPath.gridy = 0;
		panelPathLocator.add(textFieldCurrentPath, gbc_textFieldCurrentPath);
		textFieldCurrentPath.setColumns(10);

		final JPanel panelBrowseMiddle = new JPanel();
		panelBrowsing.add(panelBrowseMiddle, BorderLayout.CENTER);
		panelBrowseMiddle.setLayout(new BorderLayout(0, 0));

		table = new BadgerTable(this);

		panelBrowseMiddle.add(table, BorderLayout.CENTER);
		panelSearch = new SearchPanel(this, menuBar.getTextFieldSearch());
		contentPane.add(panelSearch, SEARCH_PANEL_NAME);
	}

	public void showCardLayoutPanel(final String panelName) {
		final CardLayout cl = (CardLayout) (contentPane.getLayout());
		cl.show(contentPane, panelName);

		searching = SEARCH_PANEL_NAME.equals(panelName);

		if (searching) {
			panelSearch.update();
		}

		update();
	}

	/**
	 * helper method for anonymous inner classes (ActionListenerImpl.) to get "this"
	 * 
	 * @return
	 */
	private JFrame getDesktopFrame() {
		return this;
	}

	public void beforeWindowClosing() {
		if (!desktopController.isInManagementMode()) {
			final Object[] options = new Object[] { "Yes", "No" };
			final int retVal = JOptionPane.showOptionDialog(getDesktopFrame(), "Disk is still opened. Do you want me to close it before we exit?",
					"Close Disk?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (retVal == JOptionPane.YES_OPTION) {
				try {
					desktopController.closeAndLogout();
				} catch (final VFSException e) {
					SwingUtil.handleException(getDesktopFrame(), e);
				}
			} else {
				return;
			}
		}

		// close the main window
		LOGGER.info("Disposing MainFrame");
		dispose();
	}

	@Override
	public void update() {
		final boolean diskMode = !desktopController.isInManagementMode();

		menuBar.update(diskMode, searching);

		contentPane.setVisible(diskMode);
		contentPane.setEnabled(diskMode);

		textFieldCurrentPath.setText(desktopController.getCurrentFolderAsString());
	}

	public DesktopController getController() {
		return desktopController;
	}

	public SearchPanel getPanelSearch() {
		return panelSearch;
	}

	@Override
	public BadgerMenuBar getJMenuBar() {
		return menuBar;
	}

	public BadgerTable getTable() {
		return table;
	}

}
