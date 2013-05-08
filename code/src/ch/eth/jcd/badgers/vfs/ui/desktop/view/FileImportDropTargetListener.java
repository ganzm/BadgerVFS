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

/**
 * Used for the drag & drop feature which allows you to import files into badger vfs
 * 
 * 
 */
public class FileImportDropTargetListener implements DropTargetListener {

	private static final Logger LOGGER = Logger.getLogger(FileImportDropTargetListener.class);

	private final DesktopController controller;

	private static final String ACCEPTED_MIME_TYPE = "application/x-java-file-list";

	public FileImportDropTargetListener(final DesktopController controller) {
		this.controller = controller;
	}

	@Override
	public void dropActionChanged(final DropTargetDragEvent arg0) {
		LOGGER.debug("dropActionChanged");
	}

	@Override
	public void drop(final DropTargetDropEvent event) {
		LOGGER.debug("drop");
		try {
			final DataFlavor[] dataFlavours = event.getCurrentDataFlavors();
			for (final DataFlavor df : dataFlavours) {

				if (acceptDataFlavour(df)) {

					event.acceptDrop(DnDConstants.ACTION_COPY);
					final Transferable transferable = event.getTransferable();
					final Object transferData = transferable.getTransferData(df);

					@SuppressWarnings("unchecked")
					final List<File> fileList = (List<File>) transferData;

					controller.startImportFromHostFs(fileList);
				}
			}
		} catch (UnsupportedFlavorException | IOException e) {
			LOGGER.error("Error while DnD", e);
		}
	}

	@Override
	public void dragOver(final DropTargetDragEvent event) {
		LOGGER.trace("dragOver");

		final DataFlavor[] dataFlavours = event.getCurrentDataFlavors();
		for (final DataFlavor df : dataFlavours) {
			if (acceptDataFlavour(df)) {
				event.acceptDrag(DnDConstants.ACTION_COPY);
			}
		}
	}

	private boolean acceptDataFlavour(final DataFlavor df) {
		final String mimeType = df.getHumanPresentableName();
		final Class<?> representationClass = df.getRepresentationClass();

		return ACCEPTED_MIME_TYPE.equals(mimeType) && representationClass == List.class;
	}

	@Override
	public void dragExit(final DropTargetEvent arg0) {
		LOGGER.debug("dragExit");
	}

	@Override
	public void dragEnter(final DropTargetDragEvent arg0) {
		LOGGER.debug("dragEnter");
	}
}
