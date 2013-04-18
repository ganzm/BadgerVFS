package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;
import ch.eth.jcd.badgers.vfs.ui.desktop.model.EntryUiModel;

public class FileExportTransferHandler extends TransferHandler {

	private static final long serialVersionUID = -2331466309042038550L;

	private static final Logger LOGGER = Logger.getLogger(FileExportTransferHandler.class);
	private final JTable tableFolderEntries;
	private final DesktopController desktopController;

	public FileExportTransferHandler(JTable tableFolderEntries, DesktopController desktopController) {
		this.tableFolderEntries = tableFolderEntries;
		this.desktopController = desktopController;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return COPY;
	}

	@Override
	public Transferable createTransferable(JComponent c) {
		LOGGER.debug("createTransferable " + c);
		return new Transferable() {

			@Override
			public boolean isDataFlavorSupported(DataFlavor df) {
				return DataFlavor.javaFileListFlavor == df;
			}

			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] { DataFlavor.javaFileListFlavor };
			}

			@Override
			public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
				if (DataFlavor.javaFileListFlavor != df) {
					throw new UnsupportedFlavorException(df);
				}

				int[] rowIndizes = tableFolderEntries.getSelectedRows();

				for (int rowIndex : rowIndizes) {

					EntryUiModel entryModel = (EntryUiModel) tableFolderEntries.getModel().getValueAt(rowIndex, 0);

				}
				//
				// List<VFSEntry> entriesToExport = new ArrayList<>();
				//
				// new vfsentryim
				//
				// ExportAction action = new ExportAction(desktopController, entriesToExport, destination, desktopFrame);

				List<File> fileList = new ArrayList<>();
				fileList.add(new File("c:\\temp\\gitrepos"));
				// TODO Auto-generated method stub
				return fileList;
			}
		};
	}

	@Override
	public void exportDone(JComponent c, Transferable t, int action) {
		LOGGER.debug("exportDone " + c + " Transferable " + t + " Action " + action);
	}
}
