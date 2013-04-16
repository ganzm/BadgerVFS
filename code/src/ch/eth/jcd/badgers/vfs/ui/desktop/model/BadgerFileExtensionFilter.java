package ch.eth.jcd.badgers.vfs.ui.desktop.model;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class BadgerFileExtensionFilter extends FileFilter {
	private final String extensions[];

	private final String description;

	public BadgerFileExtensionFilter() {
		this("Badger File System (.bfs)", ".bfs");
	}

	public BadgerFileExtensionFilter(final String description, final String extension) {
		this(description, new String[] { extension });
	}

	public BadgerFileExtensionFilter(final String description, final String extensions[]) {
		this.description = description;
		this.extensions = extensions.clone();
	}

	@Override
	public boolean accept(final File file) {
		if (file.isDirectory()) {
			return true;
		}
		final int count = extensions.length;
		final String path = file.getAbsolutePath();
		for (int i = 0; i < count; i++) {
			final String ext = extensions[i];
			if (path.endsWith(ext) && path.charAt(path.length() - ext.length()) == '.') {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		return description == null ? extensions[0] : description;
	}

}
