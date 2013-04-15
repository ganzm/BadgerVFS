package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.Component;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;

public class EntryCellEditor implements TableCellEditor {
	private static final Logger LOGGER = Logger.getLogger(EntryCellEditor.class);

	private final List<CellEditorListener> listeners = new ArrayList<CellEditorListener>();

	private final JTextField textField = new JTextField();
	private final DesktopController desktopController;
	private int currentEditedRow;
	private EntryUiModel currentEditedValue;

	private boolean allowEditing = false;
	private final JTable table;

	public EntryCellEditor(JTable table, DesktopController desktopController) {
		this.desktopController = desktopController;
		this.table = table;
	}

	/**
	 * Adds a listener to the list that's notified when the editor stops, or cancels editing.
	 */
	@Override
	public void addCellEditorListener(CellEditorListener l) {
		LOGGER.trace("EntryCellEditor - addCellEditorListener");
		listeners.add(l);
	}

	/**
	 * Removes a listener from the list that's notified
	 */
	@Override
	public void removeCellEditorListener(CellEditorListener l) {
		LOGGER.trace("EntryCellEditor - removeCellEditorListener");
		listeners.remove(l);
	}

	/**
	 * Tells the editor to cancel editing and not accept any partially edited value.
	 */
	@Override
	public void cancelCellEditing() {
		LOGGER.trace("EntryCellEditor - cancelCellEditing");
	}

	/**
	 * Returns the value contained in the editor.
	 */
	@Override
	public Object getCellEditorValue() {
		LOGGER.trace("EntryCellEditor - getCellEditorValue");
		return textField;
	}

	/**
	 * Asks the editor if it can start editing using anEvent. anEvent is in the invoking component coordinate system. The editor can not assume the Component
	 * returned by getCellEditorComponent is installed. This method is intended for the use of client to avoid the cost of setting up and installing the editor
	 * component if editing is not possible. If editing can be started this method returns true.
	 */
	@Override
	public boolean isCellEditable(EventObject eventObj) {
		LOGGER.trace("EntryCellEditor - isCellEditable");
		return allowEditing;
	}

	public void setAllowEditing(boolean allowEditing) {
		this.allowEditing = allowEditing;
	}

	/**
	 * Returns true if the editing cell should be selected, false otherwise. Typically, the return value is true, because is most cases the editing cell should
	 * be selected. However, it is useful to return false to keep the selection from changing for some types of edits. eg. A table that contains a column of
	 * check boxes, the user might want to be able to change those checkboxes without altering the selection. (See Netscape Communicator for just such an
	 * example) Of course, it is up to the client of the editor to use the return value, but it doesn't need to if it doesn't want to.
	 */
	@Override
	public boolean shouldSelectCell(EventObject arg0) {
		LOGGER.trace("EntryCellEditor - shouldSelectCell");
		return true;
	}

	/**
	 * Tells the editor to stop editing and accept any partially edited value as the value of the editor. The editor returns false if editing was not stopped;
	 * this is useful for editors that validate and can not accept invalid entries.
	 */
	@Override
	public boolean stopCellEditing() {
		LOGGER.debug("EntryCellEditor - stopCellEditing");
		if (allowEditing && !currentEditedValue.getDisplayName().equals(textField.getText())) {

			// the user changed the name of the entry
			desktopController.startRenameEntry(currentEditedValue, currentEditedRow, textField.getText());

		}
		allowEditing = false;

		table.editingStopped(new ChangeEvent(currentEditedValue));
		return true;
	}

	/**
	 * Sets an initial value for the editor. This will cause the editor to stopEditing and lose any partially edited value if the editor is editing when this
	 * method is called.
	 * 
	 * Returns the component that should be added to the client's Component hierarchy. Once installed in the client's hierarchy this component will then be able
	 * to draw and receive user input.
	 * 
	 * @param table
	 *            the JTable that is asking the editor to edit; can be null
	 * @param value
	 *            the value of the cell to be edited; it is up to the specific editor to interpret and draw the value. For example, if value is the string
	 *            "true", it could be rendered as a string or it could be rendered as a check box that is checked. null is a valid value
	 * @param isSelected
	 *            true if the cell is to be rendered with highlighting
	 * @param row
	 *            the row of the cell being edited
	 * @param column
	 *            the column of the cell being edited
	 * 
	 * @return the component for editing
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		LOGGER.trace("EntryCellEditor - getTableCellEditorComponent");

		this.currentEditedRow = row;
		this.currentEditedValue = (EntryUiModel) value;

		textField.setText(currentEditedValue.getDisplayName());
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				textField.selectAll();

			}
		});
		return textField;
	}

}
