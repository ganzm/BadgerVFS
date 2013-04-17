package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.ParentFolderEntryUiModel;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

@SuppressWarnings("serial")
public class BadgerMenuBar extends JMenuBar {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(BadgerMenuBar.class);

	public static final String DEFAULT_FIND_FIELD_STRING = "Find";

	private final JMenu mnActions;
	private final JMenuItem mntmNew;
	private final JMenuItem mntmOpen;
	private final JMenuItem mntmClose;
	private final JMenuItem mntmPaste;
	private final JMenuItem mntmQueryDiskspace;
	private final JButton btnSearch;
	private final JTextField textFieldFind;

	private final VFSSwingGui parent;

	public BadgerMenuBar(final VFSSwingGui parent) {
		this.parent = parent;
		final JMenu mnDisk = new JMenu("Disk");
		mnDisk.setMnemonic('D');
		add(mnDisk);

		mntmNew = new JMenuItem(new AbstractAction("New") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				parent.getController().openCreateNewDiskDialog(parent);
			}
		});

		mnDisk.add(mntmNew);

		mntmOpen = new JMenuItem(new AbstractAction("Open") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					parent.getController().openFileChooserForDiskOpen(parent);
				} catch (final VFSException ex) {
					SwingUtil.handleException(parent, ex);
				}
			}
		});

		mnDisk.add(mntmOpen);

		mntmClose = new JMenuItem(new AbstractAction("Close") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					parent.getController().closeDisk(parent);
					// if we open/create another disk the Browser Panel is shown
					parent.showCardLayoutPanel(VFSSwingGui.BROWSE_PANEL_NAME);
				} catch (final VFSException ex) {
					SwingUtil.handleException(parent, ex);
				}
			}
		});

		mnDisk.add(mntmClose);
		mnDisk.addSeparator();

		mntmQueryDiskspace = new JMenuItem(new AbstractAction("Query Diskspace") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				parent.getController().openDiskSpaceDialog(parent);
			}
		});
		mnDisk.add(mntmQueryDiskspace);

		mnDisk.addSeparator();

		final JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
		mnDisk.add(mntmExit);

		mnActions = new JMenu("Actions");
		mnActions.setMnemonic('A');
		add(mnActions);

		// actions on the current directory!
		// create new folder in the current directory!

		mnActions.add(new JMenuItem(new AbstractAction("New Folder") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					parent.getController().startCreateNewFolder();
				} catch (final VFSRuntimeException ex) {
					SwingUtil.handleException(parent, ex);
				}
			}
		}));

		mntmPaste = getPasteMenuItem(null);

		mnActions.add(mntmPaste);

		// open import dialog, using current folder
		mnActions.add(new JMenuItem(new AbstractAction("Import") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				parent.getController().openImportDialog(parent);
			}
		}));

		mnActions.addSeparator();

		mnActions.add(getRenameMenuItem());
		mnActions.add(getDeleteMenuItem());
		mnActions.add(getExportMenuItem());
		mnActions.add(getCopyMenuItem());
		mnActions.add(getCutMenuItem());

		final JMenu mnHelp = new JMenu("Help");
		mnHelp.setMnemonic('H');
		add(mnHelp);

		final JMenuItem mntmInfo = new JMenuItem(new AbstractAction("Info") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				parent.getController().openInfoDialog(parent);
			}
		});
		mnHelp.add(mntmInfo);

		textFieldFind = new JTextField();
		textFieldFind.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(final FocusEvent arg0) {
				if (DEFAULT_FIND_FIELD_STRING.equals(textFieldFind.getText())) {
					textFieldFind.setText("");
				}
			}

			@Override
			public void focusLost(final FocusEvent e) {
				if ("".equals(textFieldFind.getText().trim())) {
					textFieldFind.setText(DEFAULT_FIND_FIELD_STRING);
				}
			}
		});
		textFieldFind.setText(DEFAULT_FIND_FIELD_STRING);
		add(textFieldFind);
		textFieldFind.setColumns(10);

		btnSearch = new JButton(new AbstractAction("Search") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				startSearch();
			}
		});

		add(btnSearch);
	}

	private JMenuItem getPasteMenuItem(final EntryUiModel entry) {
		// paste into the current folder
		final JMenuItem mntmPaste = new JMenuItem(new AbstractAction("Paste") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				// null means use current folder
				parent.getController().pasteFromClipboardTo(entry);
			}
		});
		mntmPaste.setAccelerator(KeyStroke.getKeyStroke('V', InputEvent.CTRL_DOWN_MASK));
		return mntmPaste;
	}

	protected void startSearch() {
		parent.getPanelSearch().resetSearch();
		parent.getPanelSearch().setSearchTextAndContext(textFieldFind.getText(), parent.getController().getCurrentFolderAsString());
		parent.showCardLayoutPanel(VFSSwingGui.SEARCH_PANEL_NAME);
	}

	public void adjustActionMenus(final EntryUiModel entry) {
		mntmPaste.setEnabled(entry == null || entry.isDirectory());
	}

	public void update(final boolean diskMode, final boolean searching) {
		mnActions.setEnabled(diskMode && !searching);
		mntmClose.setEnabled(diskMode);
		mntmNew.setEnabled(!diskMode);
		mntmOpen.setEnabled(!diskMode);

		textFieldFind.setEnabled(!searching);

		mntmQueryDiskspace.setEnabled(diskMode);
		btnSearch.setEnabled(diskMode && !searching);
	}

	private JMenuItem getCutMenuItem() {
		// cuts the currently selected entries to the clipboard
		final JMenuItem retVal = new JMenuItem(new AbstractAction("Cut") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				parent.getTable().cutToClipboard();
			}
		});
		retVal.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_DOWN_MASK));
		return retVal;
	}

	private JMenuItem getCopyMenuItem() {
		// copies the currently selected entry to the clipboard
		final JMenuItem retVal = new JMenuItem(new AbstractAction("Copy") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				parent.getTable().copyToClipboard();
			}
		});
		retVal.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK));
		return retVal;
	}

	private JMenuItem getExportMenuItem() {
		// exports the currently selected entry
		final JMenuItem exportItem = new JMenuItem(new AbstractAction("Export") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				parent.getTable().startExport();
			}

		});
		exportItem.setAccelerator(KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK));
		return exportItem;
	}

	private JMenuItem getDeleteMenuItem() {
		// deletes the currently selected entry
		final JMenuItem retVal = new JMenuItem(new AbstractAction("Delete") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				parent.getTable().startDelete();
			}
		});
		retVal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		return retVal;
	}

	private JMenuItem getRenameMenuItem() {
		// renames the currently selected entry
		final JMenuItem retVal = new JMenuItem(new AbstractAction("Rename") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				parent.getTable().startRename();
			}
		});
		retVal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		return retVal;
	}

	public void doContextMenuOnTable(final MouseEvent e) {
		if (!e.isPopupTrigger()) {
			return;
		}
		LOGGER.trace(e);
		final EntryUiModel entry;
		if (e.getSource() instanceof JTable) {

			final int rowIndex = ((JTable) e.getSource()).rowAtPoint(e.getPoint());
			// rowIndex = -1 means that the user clicked in grey area where no items are --> we use the parentfolder in this case otherwise we get the item at
			// the
			// selected row
			entry = (EntryUiModel) ((JTable) e.getSource()).getValueAt(rowIndex, 0);
			// tableFolderEntries.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
		} else {
			entry = parent.getController().getParentFolderEntry();
		}
		LOGGER.debug("Opening popup on entry: " + entry.getFullPath());
		final JPopupMenu menu = new JPopupMenu();
		if (entry.isDirectory()) {

			// new folder
			menu.add(new JMenuItem(new AbstractAction("New Folder") {
				@Override
				public void actionPerformed(final ActionEvent e) {
					parent.getController().createNewFolderFromContextMenu(entry);
				}
			}));

			// paste
			menu.add(getPasteMenuItem(entry));

			// import
			menu.add(new JMenuItem(new AbstractAction("Import") {
				@Override
				public void actionPerformed(final ActionEvent e) {
					parent.getController().importFromContextMenu(entry, parent);
				}
			}));
		}

		menu.addSeparator();

		if (!(entry instanceof ParentFolderEntryUiModel)) {
			menu.add(getRenameMenuItem());
			menu.add(getDeleteMenuItem());
		}
		menu.add(getExportMenuItem());
		menu.add(getCopyMenuItem());
		menu.add(getCutMenuItem());

		menu.show(((Container) e.getSource()).getComponentAt(e.getPoint()), e.getX(), e.getY());

	}
}
