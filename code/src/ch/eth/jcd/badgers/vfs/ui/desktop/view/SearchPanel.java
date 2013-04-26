package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import ch.eth.jcd.badgers.vfs.core.model.SearchParameter;
import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.SearchAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.BadgerViewBase;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.SearchController;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public class SearchPanel extends JPanel implements BadgerViewBase {
	private static final long serialVersionUID = 6942548578015341003L;

	private SearchParameter searchParameter = new SearchParameter();
	private final JTable tableSearchResult;
	private final JTextField textFieldSearchFolder;
	private final JCheckBox chckbxSearchCaseSensitiv;
	private final JCheckBox chckbxSearchSubfolders;
	private final BadgerMainFrame parent;
	private final JButton btnStartSearch;
	private final JButton btnBack;
	private final JButton btnCancelSearch;
	private final JTextField searchTextField;
	private final SearchController searchController;
	private final JCheckBox chckbxRegexSearch;

	/**
	 * Create the panel.
	 * 
	 * @param searchTextField
	 *            GUI design decision, pass around reference to the search TextField located in BadgerMenuBar
	 */
	public SearchPanel(final BadgerMainFrame parentGui, final JTextField searchTextField) {
		this.parent = parentGui;
		this.searchTextField = searchTextField;
		this.searchController = new SearchController(parentGui.getController(), this);
		setLayout(new BorderLayout(0, 0));

		final JPanel panelSearchParameter = new JPanel();
		panelSearchParameter.setBorder(new EmptyBorder(3, 3, 3, 3));
		add(panelSearchParameter, BorderLayout.NORTH);
		final GridBagLayout gbl_panelSearchParameter = new GridBagLayout();
		gbl_panelSearchParameter.columnWidths = new int[] { 0, 94, 0, 26, 157, 0 };
		gbl_panelSearchParameter.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_panelSearchParameter.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelSearchParameter.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelSearchParameter.setLayout(gbl_panelSearchParameter);

		final JLabel lblCaseSensitiv = new JLabel("Case sensitive");
		final GridBagConstraints gbc_lblCaseSensitiv = new GridBagConstraints();
		gbc_lblCaseSensitiv.anchor = GridBagConstraints.EAST;
		gbc_lblCaseSensitiv.insets = new Insets(0, 0, 5, 5);
		gbc_lblCaseSensitiv.gridx = 0;
		gbc_lblCaseSensitiv.gridy = 0;
		panelSearchParameter.add(lblCaseSensitiv, gbc_lblCaseSensitiv);

		chckbxSearchCaseSensitiv = new JCheckBox("");
		final GridBagConstraints gbc_chckbxSearchCaseSensitiv = new GridBagConstraints();
		gbc_chckbxSearchCaseSensitiv.anchor = GridBagConstraints.WEST;
		gbc_chckbxSearchCaseSensitiv.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxSearchCaseSensitiv.gridx = 1;
		gbc_chckbxSearchCaseSensitiv.gridy = 0;
		panelSearchParameter.add(chckbxSearchCaseSensitiv, gbc_chckbxSearchCaseSensitiv);

		final JLabel lblRegex = new JLabel("Regex");
		final GridBagConstraints gbc_lblRegex = new GridBagConstraints();
		gbc_lblRegex.insets = new Insets(0, 0, 5, 5);
		gbc_lblRegex.gridx = 2;
		gbc_lblRegex.gridy = 0;
		panelSearchParameter.add(lblRegex, gbc_lblRegex);

		chckbxRegexSearch = new JCheckBox("");
		chckbxRegexSearch.setToolTipText("Enable to use regex search string");
		chckbxRegexSearch.setHorizontalAlignment(SwingConstants.LEFT);
		final GridBagConstraints gbc_chckbxRegexSearch = new GridBagConstraints();
		gbc_chckbxRegexSearch.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxRegexSearch.gridx = 3;
		gbc_chckbxRegexSearch.gridy = 0;
		panelSearchParameter.add(chckbxRegexSearch, gbc_chckbxRegexSearch);

		final JLabel lblSearchInFolder = new JLabel("Search folder");
		final GridBagConstraints gbc_lblSearchInFolder = new GridBagConstraints();
		gbc_lblSearchInFolder.anchor = GridBagConstraints.EAST;
		gbc_lblSearchInFolder.insets = new Insets(0, 0, 5, 5);
		gbc_lblSearchInFolder.gridx = 0;
		gbc_lblSearchInFolder.gridy = 1;
		panelSearchParameter.add(lblSearchInFolder, gbc_lblSearchInFolder);

		textFieldSearchFolder = new JTextField();
		final GridBagConstraints gbc_textFieldSearchFolder = new GridBagConstraints();
		gbc_textFieldSearchFolder.gridwidth = 4;
		gbc_textFieldSearchFolder.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldSearchFolder.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldSearchFolder.gridx = 1;
		gbc_textFieldSearchFolder.gridy = 1;
		panelSearchParameter.add(textFieldSearchFolder, gbc_textFieldSearchFolder);
		textFieldSearchFolder.setColumns(10);

		final JLabel lblFolderOnly = new JLabel("Search subfolders");
		final GridBagConstraints gbc_lblFolderOnly = new GridBagConstraints();
		gbc_lblFolderOnly.anchor = GridBagConstraints.EAST;
		gbc_lblFolderOnly.insets = new Insets(0, 0, 5, 5);
		gbc_lblFolderOnly.gridx = 0;
		gbc_lblFolderOnly.gridy = 2;
		panelSearchParameter.add(lblFolderOnly, gbc_lblFolderOnly);

		chckbxSearchSubfolders = new JCheckBox("");
		chckbxSearchSubfolders.setSelected(true);
		final GridBagConstraints gbc_chckbxSearchSubfolders = new GridBagConstraints();
		gbc_chckbxSearchSubfolders.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxSearchSubfolders.anchor = GridBagConstraints.WEST;
		gbc_chckbxSearchSubfolders.gridx = 1;
		gbc_chckbxSearchSubfolders.gridy = 2;
		panelSearchParameter.add(chckbxSearchSubfolders, gbc_chckbxSearchSubfolders);

		final JPanel panelButtons = new JPanel();
		final GridBagConstraints gbcPanelButtons = new GridBagConstraints();
		gbcPanelButtons.gridwidth = 4;
		gbcPanelButtons.insets = new Insets(0, 0, 0, 5);
		gbcPanelButtons.anchor = GridBagConstraints.WEST;
		gbcPanelButtons.gridx = 1;
		gbcPanelButtons.gridy = 3;
		panelSearchParameter.add(panelButtons, gbcPanelButtons);

		btnBack = new JButton("Back");
		btnBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				parent.showCardLayoutPanel(BadgerMainFrame.BROWSE_PANEL_NAME);
			}
		});
		btnBack.setMnemonic('B');
		panelButtons.add(btnBack);

		btnStartSearch = new JButton("Search");
		btnStartSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				try {
					startSearch();
				} catch (final VFSRuntimeException ex) {
					SwingUtil.handleException(getThis(), ex);
				}
			}
		});
		btnStartSearch.setMnemonic('S');
		panelButtons.add(btnStartSearch);

		btnCancelSearch = new JButton("Cancel");
		btnCancelSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				cancelSearch();
			}
		});
		panelButtons.add(btnCancelSearch);

		final JScrollPane scrollPaneSearchResult = new JScrollPane();
		add(scrollPaneSearchResult);

		tableSearchResult = new JTable();
		tableSearchResult.setDefaultRenderer(EntryUiModel.class, new EntryListCellRenderer());
		tableSearchResult.setRowHeight(40);
		tableSearchResult.setShowGrid(false);
		tableSearchResult.setModel(searchController.getSearchResultModel());
		scrollPaneSearchResult.setViewportView(tableSearchResult);

		final KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		tableSearchResult.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, "enter");
		tableSearchResult.getActionMap().put("enter", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				final int rowIndex = tableSearchResult.getSelectedRow();
				openSearchEntryAtRow(rowIndex);
			}
		});

		// mouse listeners on jtable
		tableSearchResult.addMouseListener(new MouseAdapter() {
			@Override
			// doubleclick
			public void mouseClicked(final MouseEvent event) {
				if (event.getClickCount() == 2) {
					final int rowIndex = tableSearchResult.getSelectedRow();
					openSearchEntryAtRow(rowIndex);
				}
			}
		});
	}

	protected void openSearchEntryAtRow(final int rowIndex) {
		// cancel search so the WorkerController is not busy anymore searching for files
		searchController.cancelSearch();

		// we need to load the folder of the SearchResult Entry
		searchController.openSearchEntryAtRow(rowIndex);
		parent.showCardLayoutPanel(BadgerMainFrame.BROWSE_PANEL_NAME);
	}

	protected void startSearch() {
		searchParameter.setSearchString(searchTextField.getText());
		final String searchFolder = textFieldSearchFolder.getText();
		searchParameter.setIncludeSubFolders(chckbxSearchSubfolders.isSelected());
		searchParameter.setCaseSensitive(chckbxSearchCaseSensitiv.isSelected());
		searchParameter.setRegexSearch(chckbxRegexSearch.isSelected());

		searchController.startSearch(searchParameter, searchFolder);
	}

	protected void cancelSearch() {
		searchController.cancelSearch();
		update();
	}

	public void resetSearch() {
		searchParameter = new SearchParameter();
		searchController.resetSearchResult();
	}

	public void setSearchTextAndContext(final String text, final String searchFolder) {
		searchTextField.setText(text);
		textFieldSearchFolder.setText(searchFolder);
	}

	@Override
	public void update() {
		final SearchAction currentSearchAction = searchController.getCurrentSearchAction();
		final boolean searching = currentSearchAction != null;
		final boolean canceling = currentSearchAction != null && currentSearchAction.isCanceling();

		btnCancelSearch.setEnabled(searching && !canceling);
		btnStartSearch.setEnabled(!searching);
		btnBack.setEnabled(!searching);
	}

	private SearchPanel getThis() {
		return this;
	}
}
