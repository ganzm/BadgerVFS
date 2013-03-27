package ch.eth.jcd.badgers.vfs.core;

import ch.eth.jcd.badgers.vfs.core.directory.DirectoryBlock;

public class DirectoryChildTree {
	private final DirectoryBlock rootBlock;

	public DirectoryChildTree(DirectoryBlock rootBlock) {
		this.rootBlock = rootBlock;
	}
}
