package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import ch.eth.jcd.badgers.vfs.core.model.SearchParameter;
import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.SearchAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.BadgerViewBase;
import ch.eth.jcd.badgers.vfs.ui.desktop.controller.WorkerController;

public class SearchPanel extends JPanel implements BadgerViewBase {
	private static final long serialVersionUID = 6942548578015341003L;

	private SearchParameter searchParameter = new SearchParameter();
	private SearchAction currentSearchAction = null;

	private final JTextField textFieldSearchString;
	private final JTable tableSearchResult;
	private final JTextField textFieldSearchFolder;
	private final JCheckBox chckbxSearchCaseSensitiv;
	private final JCheckBox chckbxSearchSubfolders;
	private final VFSSwingGui parent;
	private final JButton btnStartSearch;
	private final JButton btnBack;
	private final JButton btnCancelSearch;

	/**
	 * Create the panel.
	 */
	public SearchPanel(VFSSwingGui parentGui) {
		this.parent = parentGui;
		setLayout(new BorderLayout(0, 0));

		JPanel panelSearchParameter = new JPanel();
		panelSearchParameter.setBorder(new EmptyBorder(3, 3, 3, 3));
		add(panelSearchParameter, BorderLayout.NORTH);
		GridBagLayout gbl_panelSearchParameter = new GridBagLayout();
		gbl_panelSearchParameter.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelSearchParameter.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_panelSearchParameter.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelSearchParameter.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelSearchParameter.setLayout(gbl_panelSearchParameter);

		JLabel lblSearchString = new JLabel("Search String");
		GridBagConstraints gbc_lblSearchString = new GridBagConstraints();
		gbc_lblSearchString.anchor = GridBagConstraints.EAST;
		gbc_lblSearchString.insets = new Insets(0, 0, 5, 5);
		gbc_lblSearchString.gridx = 0;
		gbc_lblSearchString.gridy = 0;
		panelSearchParameter.add(lblSearchString, gbc_lblSearchString);

		textFieldSearchString = new JTextField();
		GridBagConstraints gbc_textFieldSearchString = new GridBagConstraints();
		gbc_textFieldSearchString.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldSearchString.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldSearchString.gridx = 1;
		gbc_textFieldSearchString.gridy = 0;
		panelSearchParameter.add(textFieldSearchString, gbc_textFieldSearchString);
		textFieldSearchString.setColumns(10);

		JLabel lblCaseSensitiv = new JLabel("Case sensitiv");
		GridBagConstraints gbc_lblCaseSensitiv = new GridBagConstraints();
		gbc_lblCaseSensitiv.anchor = GridBagConstraints.EAST;
		gbc_lblCaseSensitiv.insets = new Insets(0, 0, 5, 5);
		gbc_lblCaseSensitiv.gridx = 0;
		gbc_lblCaseSensitiv.gridy = 1;
		panelSearchParameter.add(lblCaseSensitiv, gbc_lblCaseSensitiv);

		chckbxSearchCaseSensitiv = new JCheckBox("");
		GridBagConstraints gbc_chckbxSearchCaseSensitiv = new GridBagConstraints();
		gbc_chckbxSearchCaseSensitiv.anchor = GridBagConstraints.WEST;
		gbc_chckbxSearchCaseSensitiv.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxSearchCaseSensitiv.gridx = 1;
		gbc_chckbxSearchCaseSensitiv.gridy = 1;
		panelSearchParameter.add(chckbxSearchCaseSensitiv, gbc_chckbxSearchCaseSensitiv);

		JLabel lblSearchInFolder = new JLabel("Search folder");
		GridBagConstraints gbc_lblSearchInFolder = new GridBagConstraints();
		gbc_lblSearchInFolder.anchor = GridBagConstraints.EAST;
		gbc_lblSearchInFolder.insets = new Insets(0, 0, 5, 5);
		gbc_lblSearchInFolder.gridx = 0;
		gbc_lblSearchInFolder.gridy = 2;
		panelSearchParameter.add(lblSearchInFolder, gbc_lblSearchInFolder);

		textFieldSearchFolder = new JTextField();
		GridBagConstraints gbc_textFieldSearchFolder = new GridBagConstraints();
		gbc_textFieldSearchFolder.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldSearchFolder.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldSearchFolder.gridx = 1;
		gbc_textFieldSearchFolder.gridy = 2;
		panelSearchParameter.add(textFieldSearchFolder, gbc_textFieldSearchFolder);
		textFieldSearchFolder.setColumns(10);

		JLabel lblFolderOnly = new JLabel("Search subfolders");
		GridBagConstraints gbc_lblFolderOnly = new GridBagConstraints();
		gbc_lblFolderOnly.anchor = GridBagConstraints.EAST;
		gbc_lblFolderOnly.insets = new Insets(0, 0, 0, 5);
		gbc_lblFolderOnly.gridx = 0;
		gbc_lblFolderOnly.gridy = 3;
		panelSearchParameter.add(lblFolderOnly, gbc_lblFolderOnly);

		chckbxSearchSubfolders = new JCheckBox("");
		GridBagConstraints gbc_chckbxSearchSubfolders = new GridBagConstraints();
		gbc_chckbxSearchSubfolders.anchor = GridBagConstraints.WEST;
		gbc_chckbxSearchSubfolders.gridx = 1;
		gbc_chckbxSearchSubfolders.gridy = 3;
		panelSearchParameter.add(chckbxSearchSubfolders, gbc_chckbxSearchSubfolders);

		JPanel panelBottom = new JPanel();
		add(panelBottom, BorderLayout.SOUTH);

		btnBack = new JButton("Back");
		btnBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				parent.showCardLayoutPanel(VFSSwingGui.BROWSE_PANEL_NAME);
			}
		});
		btnBack.setMnemonic('B');
		panelBottom.add(btnBack);

		btnStartSearch = new JButton("Search");
		btnStartSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				startSearch();
			}
		});
		btnStartSearch.setMnemonic('S');
		panelBottom.add(btnStartSearch);

		btnCancelSearch = new JButton("Cancel");
		btnCancelSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				cancelSearch();
			}
		});
		panelBottom.add(btnCancelSearch);

		JScrollPane scrollPaneSearchResult = new JScrollPane();
		add(scrollPaneSearchResult, BorderLayout.CENTER);

		tableSearchResult = new JTable();
		scrollPaneSearchResult.setViewportView(tableSearchResult);

	}

	protected void startSearch() {
		if (currentSearchAction != null) {
			throw new VFSRuntimeException("Don't do that. There is already a search in process");
		}

		searchParameter.setSearchString(textFieldSearchString.getText());
		String searchFolder = textFieldSearchFolder.getText();
		searchParameter.setIncludeSubFolders(chckbxSearchSubfolders.isSelected());
		searchParameter.setCaseSensitive(chckbxSearchCaseSensitiv.isSelected());

		WorkerController controller = WorkerController.getInstance();
		currentSearchAction = new SearchAction(searchParameter, searchFolder);
		controller.enqueue(currentSearchAction);

		update();
	}

	protected void cancelSearch() {

		if (currentSearchAction == null) {
			throw new VFSRuntimeException("Internal Error - you are not supposed to be able to cancel search");
		}

		currentSearchAction.tryCancelSearch();

		update();
	}

	public void resetSearch() {
		searchParameter = new SearchParameter();
	}

	public void setSearchTextAndContext(String text, String searchFolder) {
		textFieldSearchString.setText(text);
		textFieldSearchFolder.setText(searchFolder);
	}

	@Override
	public void update() {
		boolean searching = currentSearchAction != null;
		boolean canceling = currentSearchAction != null && currentSearchAction.isCanceling();

		btnCancelSearch.setEnabled(searching && !canceling);
		btnStartSearch.setEnabled(!searching);
		btnBack.setEnabled(!searching);
	}
}
