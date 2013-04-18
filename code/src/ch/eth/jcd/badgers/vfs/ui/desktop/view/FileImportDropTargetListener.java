package ch.eth.jcd.badgers.vfs.ui.desktop.view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;

public class FileImportDropTargetListener implements DropTargetListener {

	private static final Logger LOGGER = Logger.getLogger(FileImportDropTargetListener.class);

	private final DesktopController controller;

	private static final String acceptedMimeType = "application/x-java-file-list";

	public FileImportDropTargetListener(DesktopController controller) {
		this.controller = controller;
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent arg0) {
		LOGGER.debug("dropActionChanged");

	}

	@Override
	public void drop(DropTargetDropEvent event) {
		LOGGER.debug("drop");
		try {
			DataFlavor[] dataFlavours = event.getCurrentDataFlavors();
			for (DataFlavor df : dataFlavours) {

				if (acceptDataFlavour(df)) {

					event.acceptDrop(DnDConstants.ACTION_COPY);
					Transferable transferable = event.getTransferable();
					Object transferData = transferable.getTransferData(df);

					@SuppressWarnings("unchecked")
					List<File> fileList = (List<File>) transferData;

					controller.startImportFromHostFs(fileList);
				}
			}
		} catch (UnsupportedFlavorException | IOException e) {
			LOGGER.error("Error while DnD", e);
		}
	}

	@Override
	public void dragOver(DropTargetDragEvent event) {
		LOGGER.debug("dragOver");

		DataFlavor[] dataFlavours = event.getCurrentDataFlavors();
		for (DataFlavor df : dataFlavours) {
			if (acceptDataFlavour(df)) {
				event.acceptDrag(DnDConstants.ACTION_COPY);
			}
		}
	}

	private boolean acceptDataFlavour(DataFlavor df) {
		String mimeType = df.getHumanPresentableName();
		Class<?> representationClass = df.getRepresentationClass();
		if (acceptedMimeType.equals(mimeType)) {
			if (representationClass == List.class) {
				return true;

			}
		}
		return false;
	}

	@Override
	public void dragExit(DropTargetEvent arg0) {
		LOGGER.debug("dragExit");
	}

	@Override
	public void dragEnter(DropTargetDragEvent arg0) {
		LOGGER.debug("dragEnter");
	}
}
