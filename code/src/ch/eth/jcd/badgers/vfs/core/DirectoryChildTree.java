package ch.eth.jcd.badgers.vfs.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import ch.eth.jcd.badgers.vfs.core.directory.DirectoryBlock;
import ch.eth.jcd.badgers.vfs.core.directory.DirectoryEntryBlock;
import ch.eth.jcd.badgers.vfs.core.directory.DirectorySectionHandler;
import ch.eth.jcd.badgers.vfs.exception.VFSDuplicatedEntryException;
import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;

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
	 * @throws IOException
	 */
	public List<DirectoryEntryBlock> traverseTree(DirectorySectionHandler directorySectionhandle) throws IOException {
		List<DirectoryEntryBlock> result = new ArrayList<>();

		visitEntryBlock(directorySectionhandle, rootBlock, result);

		return result;
	}

	private void visitEntryBlock(DirectorySectionHandler directorySectionhandle, DirectoryBlock block, List<DirectoryEntryBlock> result) throws IOException {

		long dirBlockLink;
		DirectoryEntryBlock node;

		// traverse left path
		dirBlockLink = block.getLinkLeft();
		if (dirBlockLink != 0) {
			DirectoryBlock subBlock = directorySectionhandle.loadDirectoryBlock(dirBlockLink);
			visitEntryBlock(directorySectionhandle, subBlock, result);
		}

		// visit left node
		node = block.getNodeLeft();
		if (node != null) {
			result.add(node);
		}

		// traverse middle path
		dirBlockLink = block.getLinkLeft();
		if (dirBlockLink != 0) {
			DirectoryBlock subBlock = directorySectionhandle.loadDirectoryBlock(dirBlockLink);
			visitEntryBlock(directorySectionhandle, subBlock, result);
		}

		// visit right node
		node = block.getNodeRight();
		if (node != null) {
			result.add(node);
		}

		// traverse right path
		dirBlockLink = block.getLinkLeft();
		if (dirBlockLink != 0) {
			DirectoryBlock subBlock = directorySectionhandle.loadDirectoryBlock(dirBlockLink);
			visitEntryBlock(directorySectionhandle, subBlock, result);
		}
	}

	public void insert(DirectorySectionHandler directorySectionhandler, DirectoryEntryBlock newEntry) throws IOException {
		// ----------------------------
		// All insertions start at a leaf node.
		// To insert a new element, search the tree to find the leaf node where the new element should be added.
		// ----------------------------
		Stack<DirectoryBlock> pathToLeave = getBlockToInsertEntry(directorySectionhandler, newEntry);

		bottomUpTreeInsert(pathToLeave, directorySectionhandler, newEntry, null, null);
	}

	/**
	 * This is the method where we recursively insert keys from bottom to top and create new nodes where appropriate
	 * 
	 * {@link http://www.youtube.com/watch?v=coRJrcIYbF4}
	 * 
	 * @param pathToLeave
	 * @param directorySectionhandler
	 * @param newEntry
	 *            The new entry we want to insert
	 * @param lastDirectoryBlock
	 *            The directory block we were following in the last recursive call
	 * 
	 * @param directoryBlockToAttach
	 *            this direcotry block was created in a previous recursive call and should now be added to currentBlock
	 * 
	 * 
	 * @throws IOException
	 */
	private void bottomUpTreeInsert(Stack<DirectoryBlock> pathToLeave, DirectorySectionHandler directorySectionhandler, DirectoryEntryBlock newEntry,
			DirectoryBlock lastDirectoryBlock, DirectoryBlock directoryBlockToAttach) throws IOException {

		DirectoryBlock currentBlock = pathToLeave.pop();
		if (pathToLeave.isEmpty()) {
			// create new root block
			insertAtRoot(directorySectionhandler, newEntry, lastDirectoryBlock, directoryBlockToAttach);
			return;
		} else {
			currentBlock = pathToLeave.pop();

		}

		if (currentBlock.getNodeRight() == null) {
			// right block is still empty
			// this node has only 2 subnode
			// we got the DirectoryBlock where the new entry is going to belong to
			// insert Key

			// If the node contains fewer than the maximum legal number of elements, then there is room for the new element
			if (currentBlock.getNodeLeft().compareTo(newEntry) > 0) {
				currentBlock.setNodeRight(newEntry);
			} else {
				currentBlock.setNodeRight(currentBlock.getNodeLeft());
				currentBlock.setNodeLeft(newEntry);
			}

			// attach dangling DirectoryNode
			if (directoryBlockToAttach != null) {
				long toAttachLocation = directoryBlockToAttach.getLocation();
				long lastVisitedChildBlockLink = lastDirectoryBlock.getLocation();
				boolean insertDanglingOnTheRight = lastDirectoryBlock.getNodeLeft().compareTo(directoryBlockToAttach.getNodeLeft()) < 0;

				if (currentBlock.getLinkLeft() == lastVisitedChildBlockLink) {
					// recursive call ascended from the left branch
					if (insertDanglingOnTheRight) {
						currentBlock.setLinkRight(currentBlock.getLinkMiddle());
						currentBlock.setLinkMiddle(toAttachLocation);
					} else {
						currentBlock.setLinkRight(currentBlock.getLinkMiddle());
						currentBlock.setLinkMiddle(currentBlock.getLinkLeft());
						currentBlock.setLinkLeft(toAttachLocation);
					}
				} else if (currentBlock.getLinkMiddle() == lastVisitedChildBlockLink) {
					// recursive call ascended from the middle branch
					if (insertDanglingOnTheRight) {
						currentBlock.setLinkRight(toAttachLocation);
					} else {
						currentBlock.setLinkRight(currentBlock.getLinkMiddle());
						currentBlock.setLinkMiddle(toAttachLocation);
					}
				} else {
					throw new VFSRuntimeException("Internal Error - there is a problem with the DirectoryTree");
				}
			}

			directorySectionhandler.persistDirectoryBlock(currentBlock);

		} else {
			// Otherwise the node is full, evenly split it into two nodes so:
			// A single median is chosen from among the leaf's elements and the new element.

			DirectoryEntryBlock[] sorted = Sort(currentBlock.getNodeLeft(), currentBlock.getNodeRight(), newEntry);

			currentBlock.setNodeLeft(sorted[0]);
			currentBlock.setNodeRight(null);

			DirectoryBlock newBlock = directorySectionhandler.allocateNewDirectoryBlock();
			newBlock.setNodeLeft(sorted[2]);
			directorySectionhandler.persistDirectoryBlock(newBlock);

			// --------------------------
			// wire the danglink block

			if (directoryBlockToAttach != null) {
				long lastVisitedChildBlockLink = lastDirectoryBlock.getLocation();
				boolean insertDanglingOnTheRight = lastDirectoryBlock.getNodeLeft().compareTo(directoryBlockToAttach.getNodeLeft()) < 0;
				if (currentBlock.getLinkLeft() == lastVisitedChildBlockLink) {
					// recursive call ascended from the left branch

					newBlock.setLinkLeft(currentBlock.getLinkRight());
					currentBlock.setLinkRight(currentBlock.getLinkMiddle());

					if (insertDanglingOnTheRight) {
						currentBlock.setLinkMiddle(directoryBlockToAttach.getLocation());
					} else {
						currentBlock.setLinkMiddle(currentBlock.getLinkLeft());
						currentBlock.setLinkLeft(directoryBlockToAttach.getLocation());
					}

				} else if (currentBlock.getLinkMiddle() == lastVisitedChildBlockLink) {
					// recursive call ascended from the middle branch

					newBlock.setLinkLeft(currentBlock.getLinkRight());

					if (insertDanglingOnTheRight) {
						currentBlock.setLinkRight(directoryBlockToAttach.getLocation());
					} else {
						currentBlock.setLinkRight(currentBlock.getLinkMiddle());
						currentBlock.setLinkMiddle(directoryBlockToAttach.getLocation());
					}

				} else if (currentBlock.getLinkRight() == lastVisitedChildBlockLink) {
					// recursive call ascended from the right branch

					if (insertDanglingOnTheRight) {
						newBlock.setLinkLeft(directoryBlockToAttach.getLocation());
					} else {
						newBlock.setLinkLeft(currentBlock.getLinkRight());
						currentBlock.setLinkRight(directoryBlockToAttach.getLocation());
					}
				}
			}

			// save changes
			directorySectionhandler.persistDirectoryBlock(currentBlock);
			directorySectionhandler.persistDirectoryBlock(newBlock);

			// that's the median which will be inserted at our parents node
			DirectoryEntryBlock toInsertToParent = sorted[1];

			bottomUpTreeInsert(pathToLeave, directorySectionhandler, toInsertToParent, currentBlock, newBlock);
			// attach newBlock to parent and insert

		}

	}

	private void insertAtRoot(DirectorySectionHandler directorySectionhandler, DirectoryEntryBlock newEntry, DirectoryBlock lastDirectoryBlock,
			DirectoryBlock directoryBlockToAttach) throws IOException {

		DirectoryBlock newRootBlock = directorySectionhandler.allocateNewDirectoryBlock();
		newRootBlock.setNodeLeft(newEntry);

		if (directoryBlockToAttach != null) {
			// attach dangling block

			DirectoryEntryBlock entry1 = lastDirectoryBlock.getNodeLeft();
			DirectoryEntryBlock entry2 = directoryBlockToAttach.getNodeLeft();

			if (entry1.compareTo(entry2) > 0) {
				newRootBlock.setLinkLeft(lastDirectoryBlock.getLocation());
				newRootBlock.setLinkMiddle(directoryBlockToAttach.getLocation());
			} else {
				newRootBlock.setLinkLeft(directoryBlockToAttach.getLocation());
				newRootBlock.setLinkMiddle(lastDirectoryBlock.getLocation());
			}
		}

		directorySectionhandler.persistDirectoryBlock(newRootBlock);
	}

	private DirectoryEntryBlock[] Sort(DirectoryEntryBlock nodeLeft, DirectoryEntryBlock nodeRight, DirectoryEntryBlock newEntry) {
		DirectoryEntryBlock[] toSort = { nodeLeft, nodeRight, newEntry };
		Arrays.sort(toSort);
		return toSort;
	}

	/**
	 * 
	 * All insertions start at a leaf node.
	 * 
	 * To insert a new element, search the tree to find the leaf node where the new element should be added.
	 * 
	 * @param directorySectionhandler
	 * @param newEntry
	 * @return
	 * @throws IOException
	 */
	private Stack<DirectoryBlock> getBlockToInsertEntry(DirectorySectionHandler directorySectionhandler, DirectoryEntryBlock newEntry) throws IOException {

		Stack<DirectoryBlock> pathToLeave = new Stack<DirectoryBlock>();

		pathToLeave.push(rootBlock);
		DirectoryBlock currentBlock = rootBlock;

		while (true) {

			if (currentBlock.getLinkLeft() == 0) {
				// we arrived at a leave
				return pathToLeave;
			} else {
				int compLeft = currentBlock.getNodeLeft().compareTo(newEntry);

				VFSDuplicatedEntryException.throwIf(compLeft == 0);

				if (compLeft < 0) {
					// follow left branch
					currentBlock = directorySectionhandler.loadDirectoryBlock(currentBlock.getLinkLeft());
					pathToLeave.push(currentBlock);

				} else if (currentBlock.getNodeRight() != null) {
					// right node is populated
					int compRight = currentBlock.getNodeRight().compareTo(newEntry);
					VFSDuplicatedEntryException.throwIf(compRight == 0);

					if (compRight < 0) {
						// follow middle branch
						currentBlock = directorySectionhandler.loadDirectoryBlock(currentBlock.getLinkMiddle());
						pathToLeave.push(currentBlock);

					} else {
						// follow right branch
						currentBlock = directorySectionhandler.loadDirectoryBlock(currentBlock.getLinkRight());
						pathToLeave.push(currentBlock);

					}
				} else {
					// follow middle branch (since right node is not populated)
					currentBlock = directorySectionhandler.loadDirectoryBlock(currentBlock.getLinkMiddle());
					pathToLeave.push(currentBlock);

				}
			}
		}
	}
}
