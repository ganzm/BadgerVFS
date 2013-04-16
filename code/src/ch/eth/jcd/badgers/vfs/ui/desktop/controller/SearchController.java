package ch.eth.jcd.badgers.vfs.ui.desktop.controller;

import java.awt.Component;

import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.core.model.SearchParameter;
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.ActionObserver;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.BadgerAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.action.SearchAction;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryTableModel;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public class SearchController extends BadgerController implements ActionObserver {
	private static final Logger LOGGER = Logger.getLogger(SearchController.class);

	private final EntryTableModel searchResultTableModel;

	private SearchAction currentSearchAction = null;

	public SearchController(final BadgerViewBase badgerView) {
		super(badgerView);
		searchResultTableModel = new EntryTableModel();
	}

	public TableModel getSearchResultModel() {
		return searchResultTableModel;
	}

	public void startSearch(final SearchParameter searchParameter, final String searchFolder) {
		if (currentSearchAction != null) {
			throw new VFSRuntimeException("Don't do that. There is already a search in process");
		}

		final WorkerController controller = WorkerController.getInstance();
		currentSearchAction = new SearchAction(this, searchParameter, searchFolder);
		controller.enqueue(currentSearchAction);

		badgerView.update();
	}

	public SearchAction getCurrentSearchAction() {
		return currentSearchAction;
	}

	@Override
	public void onActionFailed(final BadgerAction action, final VFSException e) {
		SwingUtil.handleException((Component) badgerView, e);
	}

	@Override
	public void onActionFinished(final BadgerAction action) {
		if (action instanceof SearchAction) {
			LOGGER.debug("Search finished " + action);
			currentSearchAction = null;
			badgerView.update();
		} else {
			SwingUtil.handleError((Component) badgerView, "Unexpected Action " + action);
		}
	}

	public void cancelSearch() {
		if (currentSearchAction == null) {
			throw new VFSRuntimeException("Internal Error - you are not supposed to be able to cancel search");
		}

		currentSearchAction.tryCancelSearch();
	}
}
