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
import javax.swing.border.EmptyBorder;

import ch.eth.jcd.badgers.vfs.core.model.SearchParameter;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.SearchAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.BadgerViewBase;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.SearchController;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;

public class SearchPanel extends JPanel implements BadgerViewBase {
	private static final long serialVersionUID = 6942548578015341003L;

	private SearchParameter searchParameter = new SearchParameter();
	private final JTextField textFieldSearchString;
	private final JTable tableSearchResult;
	private final JTextField textFieldSearchFolder;
	private final JCheckBox chckbxSearchCaseSensitiv;
	private final JCheckBox chckbxSearchSubfolders;
	private final VFSSwingGui parent;
	private final JButton btnStartSearch;
	private final JButton btnBack;
	private final JButton btnCancelSearch;

	private final SearchController searchController;

	/**
	 * Create the panel.
	 */
	public SearchPanel(final VFSSwingGui parentGui) {
		this.parent = parentGui;
		this.searchController = new SearchController(parentGui.getController(), this);
		setLayout(new BorderLayout(0, 0));

		final JPanel panelSearchParameter = new JPanel();
		panelSearchParameter.setBorder(new EmptyBorder(3, 3, 3, 3));
		add(panelSearchParameter, BorderLayout.NORTH);
		final GridBagLayout gbl_panelSearchParameter = new GridBagLayout();
		gbl_panelSearchParameter.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelSearchParameter.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_panelSearchParameter.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelSearchParameter.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelSearchParameter.setLayout(gbl_panelSearchParameter);

		final JLabel lblSearchString = new JLabel("Search String");
		final GridBagConstraints gbc_lblSearchString = new GridBagConstraints();
		gbc_lblSearchString.anchor = GridBagConstraints.EAST;
		gbc_lblSearchString.insets = new Insets(0, 0, 5, 5);
		gbc_lblSearchString.gridx = 0;
		gbc_lblSearchString.gridy = 0;
		panelSearchParameter.add(lblSearchString, gbc_lblSearchString);

		textFieldSearchString = new JTextField();
		final GridBagConstraints gbc_textFieldSearchString = new GridBagConstraints();
		gbc_textFieldSearchString.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldSearchString.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldSearchString.gridx = 1;
		gbc_textFieldSearchString.gridy = 0;
		panelSearchParameter.add(textFieldSearchString, gbc_textFieldSearchString);
		textFieldSearchString.setColumns(10);

		final JLabel lblCaseSensitiv = new JLabel("Case sensitiv");
		final GridBagConstraints gbc_lblCaseSensitiv = new GridBagConstraints();
		gbc_lblCaseSensitiv.anchor = GridBagConstraints.EAST;
		gbc_lblCaseSensitiv.insets = new Insets(0, 0, 5, 5);
		gbc_lblCaseSensitiv.gridx = 0;
		gbc_lblCaseSensitiv.gridy = 1;
		panelSearchParameter.add(lblCaseSensitiv, gbc_lblCaseSensitiv);

		chckbxSearchCaseSensitiv = new JCheckBox("");
		final GridBagConstraints gbc_chckbxSearchCaseSensitiv = new GridBagConstraints();
		gbc_chckbxSearchCaseSensitiv.anchor = GridBagConstraints.WEST;
		gbc_chckbxSearchCaseSensitiv.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxSearchCaseSensitiv.gridx = 1;
		gbc_chckbxSearchCaseSensitiv.gridy = 1;
		panelSearchParameter.add(chckbxSearchCaseSensitiv, gbc_chckbxSearchCaseSensitiv);

		final JLabel lblSearchInFolder = new JLabel("Search folder");
		final GridBagConstraints gbc_lblSearchInFolder = new GridBagConstraints();
		gbc_lblSearchInFolder.anchor = GridBagConstraints.EAST;
		gbc_lblSearchInFolder.insets = new Insets(0, 0, 5, 5);
		gbc_lblSearchInFolder.gridx = 0;
		gbc_lblSearchInFolder.gridy = 2;
		panelSearchParameter.add(lblSearchInFolder, gbc_lblSearchInFolder);

		textFieldSearchFolder = new JTextField();
		final GridBagConstraints gbc_textFieldSearchFolder = new GridBagConstraints();
		gbc_textFieldSearchFolder.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldSearchFolder.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldSearchFolder.gridx = 1;
		gbc_textFieldSearchFolder.gridy = 2;
		panelSearchParameter.add(textFieldSearchFolder, gbc_textFieldSearchFolder);
		textFieldSearchFolder.setColumns(10);

		final JLabel lblFolderOnly = new JLabel("Search subfolders");
		final GridBagConstraints gbc_lblFolderOnly = new GridBagConstraints();
		gbc_lblFolderOnly.anchor = GridBagConstraints.EAST;
		gbc_lblFolderOnly.insets = new Insets(0, 0, 0, 5);
		gbc_lblFolderOnly.gridx = 0;
		gbc_lblFolderOnly.gridy = 3;
		panelSearchParameter.add(lblFolderOnly, gbc_lblFolderOnly);

		chckbxSearchSubfolders = new JCheckBox("");
		final GridBagConstraints gbc_chckbxSearchSubfolders = new GridBagConstraints();
		gbc_chckbxSearchSubfolders.anchor = GridBagConstraints.WEST;
		gbc_chckbxSearchSubfolders.gridx = 1;
		gbc_chckbxSearchSubfolders.gridy = 3;
		panelSearchParameter.add(chckbxSearchSubfolders, gbc_chckbxSearchSubfolders);

		final JPanel panelBottom = new JPanel();
		add(panelBottom, BorderLayout.SOUTH);

		btnBack = new JButton("Back");
		btnBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				parent.showCardLayoutPanel(VFSSwingGui.BROWSE_PANEL_NAME);
			}
		});
		btnBack.setMnemonic('B');
		panelBottom.add(btnBack);

		btnStartSearch = new JButton("Search");
		btnStartSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				startSearch();
			}
		});
		btnStartSearch.setMnemonic('S');
		panelBottom.add(btnStartSearch);

		btnCancelSearch = new JButton("Cancel");
		btnCancelSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				cancelSearch();
			}
		});
		panelBottom.add(btnCancelSearch);

		final JScrollPane scrollPaneSearchResult = new JScrollPane();
		add(scrollPaneSearchResult, BorderLayout.CENTER);

		tableSearchResult = new JTable();
		tableSearchResult.setDefaultRenderer(EntryUiModel.class, new EntryListCellRenderer());
		tableSearchResult.setRowHeight(40);
		tableSearchResult.setShowGrid(false);
		tableSearchResult.setModel(searchController.getSearchResultModel());
		scrollPaneSearchResult.setViewportView(tableSearchResult);

		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		tableSearchResult.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, "enter");
		tableSearchResult.getActionMap().put("enter", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int rowIndex = tableSearchResult.getSelectedRow();
				openSearchEntryAtRow(rowIndex);
			}
		});

		// mouse listeners on jtable
		tableSearchResult.addMouseListener(new MouseAdapter() {
			@Override
			// doubleclick
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() == 2) {
					int rowIndex = tableSearchResult.getSelectedRow();
					openSearchEntryAtRow(rowIndex);
				}
			}
		});
	}

	protected void openSearchEntryAtRow(int rowIndex) {
		searchController.openSearchEntryAtRow(rowIndex);
		parent.showCardLayoutPanel(VFSSwingGui.BROWSE_PANEL_NAME);
	}

	protected void startSearch() {
		searchParameter.setSearchString(textFieldSearchString.getText());
		final String searchFolder = textFieldSearchFolder.getText();
		searchParameter.setIncludeSubFolders(chckbxSearchSubfolders.isSelected());
		searchParameter.setCaseSensitive(chckbxSearchCaseSensitiv.isSelected());

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
		textFieldSearchString.setText(text);
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

}
