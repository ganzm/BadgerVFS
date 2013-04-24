package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import java.awt.Component;

import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.model.SearchParameter;
import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.AbstractBadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.OpenFileInFolderAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.disk.SearchAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryTableModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public class SearchController extends BadgerController implements ActionObserver {
	private static final Logger LOGGER = Logger.getLogger(SearchController.class);

	private final EntryTableModel searchResultTableModel;

	private SearchAction currentSearchAction = null;

	private final DesktopController parentController;

	public SearchController(final DesktopController parentController, final BadgerViewBase badgerView) {
		super(badgerView);
		this.parentController = parentController;
		searchResultTableModel = new EntryTableModel();
	}

	public TableModel getSearchResultModel() {
		return searchResultTableModel;
	}

	public void startSearch(final SearchParameter searchParameter, final String searchFolder) {
		if (currentSearchAction != null) {
			throw new VFSRuntimeException("Don't do that. There is already a search in process");
		}

		searchResultTableModel.clear();

		final WorkerController controller = parentController.getWorkerController();
		currentSearchAction = new SearchAction(this, searchParameter, searchFolder);
		controller.enqueue(currentSearchAction);

		updateGUI();
	}

	public SearchAction getCurrentSearchAction() {
		return currentSearchAction;
	}

	@Override
	public void onActionFailed(final AbstractBadgerAction action, final Exception e) {
		if (action instanceof SearchAction) {
			currentSearchAction = null;
		}

		SwingUtil.handleException((Component) getView(), e);
		updateGUI();
	}

	@Override
	public void onActionFinished(final AbstractBadgerAction action) {
		if (action instanceof SearchAction) {
			LOGGER.debug("Search finished " + action);
			currentSearchAction = null;
			updateGUI();
		} else {
			SwingUtil.handleError((Component) getView(), "Unexpected Action " + action);
		}
	}

	public void cancelSearch() {
		if (currentSearchAction == null) {
			return;
		}

		currentSearchAction.tryCancelSearch();
	}

	public void foundEntry(final EntryUiModel entryModel) {
		searchResultTableModel.appendEntry(entryModel);
	}

	public void openSearchEntryAtRow(final int rowIndex) {
		final EntryUiModel entryModel = (EntryUiModel) searchResultTableModel.getValueAt(rowIndex, 0);
		final OpenFileInFolderAction action = new OpenFileInFolderAction(parentController, entryModel);
		parentController.getWorkerController().enqueue(action);
	}

	public void resetSearchResult() {
		searchResultTableModel.clear();
	}
}
