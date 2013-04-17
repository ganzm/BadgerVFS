package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.ui.desktop.Initialisation;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.BadgerViewBase;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.ParentFolderEntryUiModel;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

@SuppressWarnings("serial")
public class VFSSwingGui extends JFrame implements BadgerViewBase {

	private static final long serialVersionUID = -8776317677851635247L;

	private static final Logger LOGGER = Logger.getLogger(VFSSwingGui.class);
	public static final String BROWSE_PANEL_NAME = "browsepanel";
	public static final String SEARCH_PANEL_NAME = "searchpanel";

	private final EntryCellEditor entryCellEditor;

	private final JPanel contentPane;

	private final DesktopController desktopController = new DesktopController(this);

	private final JTextField textFieldCurrentPath;
	private final JTable tableFolderEntries;

	private final SearchPanel panelSearch;
	/**
	 * State Variable determines whether search or browse gui is shown
	 */
	private boolean searching = false;

	private final BadgerMenuBar menuBar;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		Initialisation.initApplication(args);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final VFSSwingGui frame = new VFSSwingGui();
				frame.update();
				frame.setVisible(true);
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public VFSSwingGui() {
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

		// ADDING "Enter" handler on table item
		final KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

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

		final JScrollPane scrollPaneBrowseTable = new JScrollPane();
		scrollPaneBrowseTable.addMouseListener(new MouseAdapter() {

			@Override
			// rightclick windows/linux
			public void mousePressed(final MouseEvent e) {
				menuBar.doContextMenuOnTable(e);
			}

			@Override
			// rightclick mac
			public void mouseReleased(final MouseEvent e) {
				menuBar.doContextMenuOnTable(e);
			}
		});
		panelBrowseMiddle.add(scrollPaneBrowseTable, BorderLayout.CENTER);

		tableFolderEntries = new JTable();
		tableFolderEntries.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPaneBrowseTable.setViewportView(tableFolderEntries);
		tableFolderEntries.setShowGrid(false);
		tableFolderEntries.setDefaultRenderer(EntryUiModel.class, new EntryListCellRenderer());
		tableFolderEntries.setRowHeight(40);
		tableFolderEntries.setModel(desktopController.getEntryTableModel());

		final TableColumn columnModel = tableFolderEntries.getColumnModel().getColumn(0);
		entryCellEditor = new EntryCellEditor(tableFolderEntries, desktopController);
		columnModel.setCellEditor(entryCellEditor);

		// mouse listeners on jtable
		tableFolderEntries.addMouseListener(new MouseAdapter() {

			@Override
			// doubleclick
			public void mouseClicked(final MouseEvent event) {
				if (event.getClickCount() >= 2) {
					final int rowIndex = tableFolderEntries.rowAtPoint(event.getPoint());
					final EntryUiModel entry = (EntryUiModel) tableFolderEntries.getModel().getValueAt(rowIndex, 0);
					LOGGER.debug("Doubleclicked " + entry);
					if (entry != null && entry.isDirectory()) {
						desktopController.openEntry(entry);
					}
				}
			}

			@Override
			// rightclick windows/linux
			public void mousePressed(final MouseEvent e) {
				menuBar.doContextMenuOnTable(e);
			}

			@Override
			// rightclick mac
			public void mouseReleased(final MouseEvent e) {
				menuBar.doContextMenuOnTable(e);
			}

		});

		// adjusting menuItems when selection in table changes
		tableFolderEntries.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				final EntryUiModel entry = (EntryUiModel) tableFolderEntries.getValueAt(tableFolderEntries.getSelectedRow(), 0);
				menuBar.adjustActionMenus(entry);
			}

		});
		tableFolderEntries.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, "enter");
		tableFolderEntries.getActionMap().put("enter", new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (tableFolderEntries.isEditing()) {
					// we are currently renaming an entry
					// hitting Enter should stop that
					entryCellEditor.stopCellEditing();
					return;
				}
				final EntryUiModel entry = (EntryUiModel) tableFolderEntries.getValueAt(tableFolderEntries.getSelectedRow(), 0);
				if (entry != null) {
					if (entry.isDirectory()) {
						desktopController.openEntry(entry);
					} else {
						desktopController.startExport(getDesktopFrame(), Arrays.asList(new EntryUiModel[] { entry }));
					}
				}
			}
		});

		removeKeysFromJTableInputMap(tableFolderEntries);

		panelSearch = new SearchPanel(this);
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
	 * copied from http://stackoverflow.com/questions/2019371/swing-setting-a-function-key-f2-as-an-accelerator
	 * 
	 * @param tableFolderEntries2
	 */
	private static void removeKeysFromJTableInputMap(final JTable table) {
		final KeyStroke f2KeyToRemove = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
		final KeyStroke deleteKeyToRemove = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		final KeyStroke ctrlC = KeyStroke.getKeyStroke('C', KeyEvent.CTRL_DOWN_MASK);
		final KeyStroke ctrlX = KeyStroke.getKeyStroke('X', KeyEvent.CTRL_DOWN_MASK);
		final KeyStroke ctrlV = KeyStroke.getKeyStroke('V', KeyEvent.CTRL_DOWN_MASK);

		InputMap imap = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		while (imap != null) {
			imap.remove(f2KeyToRemove);
			imap.remove(deleteKeyToRemove);
			imap.remove(ctrlC);
			imap.remove(ctrlX);
			imap.remove(ctrlV);
			imap = imap.getParent();
		}
	}

	public List<EntryUiModel> getSelectedEntries() {
		final int[] selectedRowIdexes = tableFolderEntries.getSelectedRows();
		final List<EntryUiModel> selectedEntries = new ArrayList<EntryUiModel>(selectedRowIdexes.length);
		for (final int i : selectedRowIdexes) {
			final EntryUiModel uiEntry = (EntryUiModel) tableFolderEntries.getValueAt(i, 0);
			if (!(uiEntry instanceof ParentFolderEntryUiModel)) {
				selectedEntries.add(uiEntry);
			}
		}
		return selectedEntries;
	}

	/**
	 * helper method for anonymous inner classes (ActionListenerImpl.) to get "this"
	 * 
	 * @return
	 */
	private JFrame getDesktopFrame() {
		return this;
	}

	private void beforeWindowClosing() {
		if (!desktopController.isInManagementMode()) {
			final Object[] options = new Object[] { "Yes", "No" };
			final int retVal = JOptionPane.showOptionDialog(getDesktopFrame(), "Disk is still opened. Do you want me to close it before we exit?",
					"Close Disk?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (retVal == JOptionPane.YES_OPTION) {
				try {
					desktopController.closeDisk(getDesktopFrame());
				} catch (final VFSException e) {
					SwingUtil.handleException(getDesktopFrame(), e);
				}
			} else {
				return;
			}
		}

		// close the main window
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

	public JTable getTableFolderEntries() {
		return tableFolderEntries;
	}

	public EntryCellEditor getEntryCellEditor() {
		return entryCellEditor;
	}

}
