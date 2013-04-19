package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.ParentFolderEntryUiModel;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public class BadgerTable extends JScrollPane {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(BadgerTable.class);
	private final JTable tableFolderEntries;
	private final EntryCellEditor entryCellEditor;
	private final BadgerMainFrame parent;

	public BadgerTable(final BadgerMainFrame parent) {
		this.parent = parent;
		addMouseListener(new MouseAdapter() {

			@Override
			// rightclick windows/linux
			public void mousePressed(final MouseEvent e) {
				parent.getJMenuBar().doContextMenuOnTable(e);
			}

			@Override
			// rightclick mac
			public void mouseReleased(final MouseEvent e) {
				parent.getJMenuBar().doContextMenuOnTable(e);
			}
		});

		tableFolderEntries = new JTable();
		tableFolderEntries.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setViewportView(tableFolderEntries);
		tableFolderEntries.setShowGrid(false);
		tableFolderEntries.setDefaultRenderer(EntryUiModel.class, new EntryListCellRenderer());
		tableFolderEntries.setRowHeight(40);
		tableFolderEntries.setModel(parent.getController().getEntryTableModel());

		final TableColumn columnModel = tableFolderEntries.getColumnModel().getColumn(0);
		entryCellEditor = new EntryCellEditor(tableFolderEntries, parent.getController());
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
						parent.getController().openEntry(entry);
					}
				}
			}

			@Override
			// rightclick windows/linux
			public void mousePressed(final MouseEvent e) {
				parent.getJMenuBar().doContextMenuOnTable(e);
			}

			@Override
			// rightclick mac
			public void mouseReleased(final MouseEvent e) {
				parent.getJMenuBar().doContextMenuOnTable(e);
			}

		});

		// adjusting menuItems when selection in table changes
		tableFolderEntries.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				final EntryUiModel entry = (EntryUiModel) tableFolderEntries.getValueAt(tableFolderEntries.getSelectedRow(), 0);
				parent.getJMenuBar().adjustActionMenus(entry);
			}

		});

		// ADDING "Enter" handler on table item
		final KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
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
						parent.getController().openEntry(entry);
					} else {
						parent.getController().startExport(parent, Arrays.asList(new EntryUiModel[] { entry }));
					}
				}
			}
		});

		tableFolderEntries.setDragEnabled(true);
		new DropTarget(this, new FileImportDropTargetListener(parent.getController()));
		removeKeysFromJTableInputMap(tableFolderEntries);
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

	private List<EntryUiModel> getSelectedEntries() {
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

	public void startRename() {
		try {
			final int currentRow = tableFolderEntries.getSelectedRow();
			if (currentRow < 0) {
				JOptionPane.showMessageDialog(this, "Selecte File or Folder", "Badger Message", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			final EntryUiModel entry = (EntryUiModel) tableFolderEntries.getValueAt(currentRow, 0);
			LOGGER.debug("Start renaming " + entry);

			entryCellEditor.setAllowEditing(true);
			tableFolderEntries.editCellAt(currentRow, 0);
		} catch (final VFSRuntimeException ex) {
			SwingUtil.handleException(parent, ex);
		}
	}

	public void startDelete() {
		try {
			final List<EntryUiModel> selectedEntries = getSelectedEntries();
			if (selectedEntries.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Select File or Folder", "Badger Message", JOptionPane.INFORMATION_MESSAGE);
				return;

			}
			parent.getController().startDelete(selectedEntries);
		} catch (final VFSRuntimeException ex) {
			SwingUtil.handleException(parent, ex);
		}
	}

	public void startExport() {
		final List<EntryUiModel> selectedItems = getSelectedEntries();
		if (selectedItems.isEmpty()) {
			selectedItems.add(parent.getController().getParentFolderEntry());
		}

		parent.getController().startExport(parent, selectedItems);
	}

	public void copyToClipboard() {
		parent.getController().copyToClipboard(getSelectedEntries());
	}

	public void cutToClipboard() {
		parent.getController().cutToClipboard(getSelectedEntries());

	}
}
