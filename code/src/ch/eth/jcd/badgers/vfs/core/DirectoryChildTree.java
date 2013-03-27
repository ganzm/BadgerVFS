package ch.eth.jcd.badgers.vfs.core;

import java.util.ArrayList;
import java.util.List;

import ch.eth.jcd.badgers.vfs.core.directory.DirectoryBlock;
import ch.eth.jcd.badgers.vfs.core.directory.DirectoryEntryBlock;
import ch.eth.jcd.badgers.vfs.core.directory.DirectorySectionHandler;

public class DirectoryChildTree {

	/**
	 * upper most node of our tree
	 */
	private final DirectoryBlock rootBlock;

	public DirectoryChildTree(DirectoryBlock rootBlock) {
		this.rootBlock = rootBlock;
	}

	/**
	 * depth first tree traversal
	 * 
	 * @param diskManager
	 * 
	 * @return
	 */
	public List<DirectoryEntryBlock> traverseTree(DirectorySectionHandler directorySectionhandle) {
		List<DirectoryEntryBlock> result = new ArrayList<>();
		//
		// Stack<DirectoryBlock> stack = new Stack<>();
		//
		// stack.push(rootBlock);
		//
		// while(stack.isEmpty() == false){
		// DirectoryBlock current = stack.peek();
		//
		// if(current.getLeft() != 0){
		// // travers/push left branch
		// stack.push(current.getLeft());
		// }
		// else if (current.getMiddle() != 0){
		// // travers/push middle branch
		// stack.push(current.getMiddle());
		// }
		// else if (current.getRight() != 0){
		// // travers/push right branch
		// stack.push(current.getRight());
		// }
		// else {
		// // visit current
		// current.
		//
		// // throw away the element
		// stack.pop();
		// }
		// }
		//
		// directorySectionhandle.loadDirectoryBlock(rootBlock.getLocation());
		//
		// asdf;
		//
		//

		return result;
	}
}
