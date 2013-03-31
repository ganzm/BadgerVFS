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
import ch.eth.jcd.badgers.vfs.exception.VFSException;
import ch.eth.jcd.badgers.vfs.exception.VFSInvalidLocationExceptionException;
import ch.eth.jcd.badgers.vfs.exception.VFSRuntimeException;

/**
 * $Id$
 * 
 * b tree which contains the child entries of a single directory
 * 
 */
public class DirectoryChildTree {

	/**
	 * upper most node of our tree
	 */
	private DirectoryBlock rootBlock;

	public DirectoryChildTree(DirectoryBlock rootBlock) {
		this.rootBlock = rootBlock;
	}

	public DirectoryBlock getRootBlock() {
		return rootBlock;
	}

	/**
	 * depth first tree traversal
	 * 
	 * @param diskManager
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<DirectoryEntryBlock> traverseTree(DirectorySectionHandler directorySectionhandler) throws VFSException {
		List<DirectoryEntryBlock> result = new ArrayList<>();

		visitEntryBlock(directorySectionhandler, rootBlock, result);

		return result;
	}

	private void visitEntryBlock(DirectorySectionHandler directorySectionhandler, DirectoryBlock block, List<DirectoryEntryBlock> result) throws VFSException,
			VFSInvalidLocationExceptionException {

		long dirBlockLink;
		DirectoryEntryBlock node;

		// traverse left path
		dirBlockLink = block.getLinkLeft();
		if (dirBlockLink != 0) {
			DirectoryBlock subBlock = directorySectionhandler.loadDirectoryBlock(dirBlockLink);
			visitEntryBlock(directorySectionhandler, subBlock, result);
		}

		// visit left node
		node = block.getNodeLeft();
		if (node != null) {
			result.add(node);
		}

		// traverse middle path
		dirBlockLink = block.getLinkMiddle();
		if (dirBlockLink != 0) {
			DirectoryBlock subBlock = directorySectionhandler.loadDirectoryBlock(dirBlockLink);
			visitEntryBlock(directorySectionhandler, subBlock, result);
		}

		// visit right node
		node = block.getNodeRight();
		if (node != null) {
			result.add(node);
		}

		// traverse right path
		dirBlockLink = block.getLinkRight();
		if (dirBlockLink != 0) {
			DirectoryBlock subBlock = directorySectionhandler.loadDirectoryBlock(dirBlockLink);
			visitEntryBlock(directorySectionhandler, subBlock, result);
		}
	}

	public void insert(DirectorySectionHandler directorySectionhandler, DirectoryEntryBlock newEntry) throws VFSException, IOException {
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
	 *            this directory block was created in a previous recursive call and should now be added to currentBlock
	 * 
	 * 
	 * @throws IOException
	 */
	private void bottomUpTreeInsert(Stack<DirectoryBlock> pathToLeave, DirectorySectionHandler directorySectionhandler, DirectoryEntryBlock newEntry,
			DirectoryBlock lastDirectoryBlock, DirectoryBlock directoryBlockToAttach) throws IOException {

		if (pathToLeave.isEmpty()) {
			insertAtRoot(directorySectionhandler, newEntry, lastDirectoryBlock, directoryBlockToAttach);
			return;
		}

		DirectoryBlock currentBlock = pathToLeave.pop();

		if (currentBlock.getNodeLeft() == null) {
			// this is the very first insert into this b-tree
			currentBlock.setNodeLeft(newEntry);
			directorySectionhandler.persistDirectoryBlock(currentBlock);

		} else if (currentBlock.getNodeRight() == null) {
			// right block is still empty
			// this node has only 2 subnode
			// we got the DirectoryBlock where the new entry is going to belong to
			// insert Key

			// If the node contains fewer than the maximum legal number of elements, then there is room for the new element
			if (currentBlock.getNodeLeft().compareTo(newEntry) < 0) {
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
			// wire the dangling block

			if (directoryBlockToAttach != null) {
				long lastVisitedChildBlockLink = lastDirectoryBlock.getLocation();
				boolean insertDanglingOnTheRight = lastDirectoryBlock.getNodeLeft().compareTo(directoryBlockToAttach.getNodeLeft()) < 0;
				if (currentBlock.getLinkLeft() == lastVisitedChildBlockLink) {
					// recursive call ascended from the left branch

					newBlock.setLinkLeft(currentBlock.getLinkMiddle());
					newBlock.setLinkMiddle(currentBlock.getLinkRight());

					if (insertDanglingOnTheRight) {
						currentBlock.setLinkMiddle(directoryBlockToAttach.getLocation());
					} else {
						currentBlock.setLinkMiddle(currentBlock.getLinkLeft());
						currentBlock.setLinkLeft(directoryBlockToAttach.getLocation());
					}

					currentBlock.setLinkRight(0);

				} else if (currentBlock.getLinkMiddle() == lastVisitedChildBlockLink) {
					// recursive call ascended from the middle branch

					if (insertDanglingOnTheRight) {
						newBlock.setLinkLeft(directoryBlockToAttach.getLocation());

						newBlock.setLinkMiddle(currentBlock.getLinkRight());
					} else {
						newBlock.setLinkLeft(currentBlock.getLinkMiddle());
						newBlock.setLinkMiddle(currentBlock.getLinkRight());

						currentBlock.setLinkMiddle(directoryBlockToAttach.getLocation());
					}

					currentBlock.setLinkRight(0);

				} else if (currentBlock.getLinkRight() == lastVisitedChildBlockLink) {
					// recursive call ascended from the right branch

					if (insertDanglingOnTheRight) {
						newBlock.setLinkLeft(currentBlock.getLinkRight());
						newBlock.setLinkMiddle(directoryBlockToAttach.getLocation());
					} else {
						newBlock.setLinkMiddle(currentBlock.getLinkRight());
						newBlock.setLinkLeft(directoryBlockToAttach.getLocation());
					}

					currentBlock.setLinkRight(0);
				}
			}

			// save changes
			directorySectionhandler.persistDirectoryBlock(currentBlock);
			directorySectionhandler.persistDirectoryBlock(newBlock);

			// that's the median which will be inserted at our parents node
			DirectoryEntryBlock toInsertToParent = sorted[1];

			bottomUpTreeInsert(pathToLeave, directorySectionhandler, toInsertToParent, currentBlock, newBlock);
		}
	}

	private void insertAtRoot(DirectorySectionHandler directorySectionhandler, DirectoryEntryBlock newEntry, DirectoryBlock lastDirectoryBlock,
			DirectoryBlock directoryBlockToAttach) throws IOException {

		DirectoryBlock newRootBlock = directorySectionhandler.allocateNewDirectoryBlock();
		newRootBlock.setNodeLeft(newEntry);

		if (lastDirectoryBlock.getNodeLeft().compareTo(directoryBlockToAttach.getNodeLeft()) < 0) {

			newRootBlock.setLinkLeft(lastDirectoryBlock.getLocation());
			newRootBlock.setLinkMiddle(directoryBlockToAttach.getLocation());
		} else {

			newRootBlock.setLinkLeft(directoryBlockToAttach.getLocation());
			newRootBlock.setLinkMiddle(lastDirectoryBlock.getLocation());
		}

		directorySectionhandler.persistDirectoryBlock(newRootBlock);

		this.rootBlock = newRootBlock;
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
	 * @throws VFSInvalidLocationExceptionException
	 */
	private Stack<DirectoryBlock> getBlockToInsertEntry(DirectorySectionHandler directorySectionhandler, DirectoryEntryBlock newEntry) throws VFSException,
			VFSInvalidLocationExceptionException {

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

				if (compLeft > 0) {
					// follow left branch
					currentBlock = directorySectionhandler.loadDirectoryBlock(currentBlock.getLinkLeft());
					pathToLeave.push(currentBlock);

				} else if (currentBlock.getNodeRight() != null) {
					// right node is populated
					int compRight = currentBlock.getNodeRight().compareTo(newEntry);
					VFSDuplicatedEntryException.throwIf(compRight == 0);

					if (compRight > 0) {
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

	/**
	 * 
	 * @param directorySectionHandler
	 * @param fileName
	 * @return DirectoryEntryBlock which was removed
	 * @throws IOException
	 */
	public void remove(DirectorySectionHandler directorySectionHandler, String fileName) throws VFSException, IOException {

		// dummy block
		DirectoryEntryBlock fileNameBlock = new DirectoryEntryBlock(fileName);

		Stack<DirectoryBlock> pathTopDown = findDirectoryBlockWithEntry(directorySectionHandler, fileNameBlock);

		DirectoryBlock currentDirectoryBlock = pathTopDown.peek();

		long linkLocation = 0;
		boolean deleteLeftNode;
		if (currentDirectoryBlock.getNodeLeft().compareTo(fileNameBlock) == 0) {
			// we want to delete the left Entry of the currentDirectoryBlock
			linkLocation = currentDirectoryBlock.getLinkLeft();
			deleteLeftNode = true;
		} else {
			// we want to delete the right Entry of the currentDirectoryBlock
			linkLocation = currentDirectoryBlock.getLinkMiddle();
			deleteLeftNode = false;
		}

		if (linkLocation == 0) {
			// we are trying to remove an entry from a leave

			// simply delete the entry
			if (deleteLeftNode) {
				currentDirectoryBlock.setNodeLeft(currentDirectoryBlock.getNodeRight());
				currentDirectoryBlock.setNodeRight(null);
			} else {
				currentDirectoryBlock.setNodeRight(null);
			}
			directorySectionHandler.persistDirectoryBlock(currentDirectoryBlock);

			// rebalance the tree
			rebalanceTreeAfterDeletion(directorySectionHandler, pathTopDown, fileNameBlock);

		} else {
			// we are trying to remove an internal node of our tree

			Stack<DirectoryBlock> pathToSymmetricFollower = new Stack<>();
			DirectoryEntryBlock symFollower = findAndDeleteSymmetricFollower(directorySectionHandler, linkLocation, pathToSymmetricFollower);

			// replace node to delete with symmetric follower
			if (deleteLeftNode) {
				currentDirectoryBlock.setNodeLeft(symFollower);
			} else {
				currentDirectoryBlock.setNodeRight(symFollower);
			}
			directorySectionHandler.persistDirectoryBlock(currentDirectoryBlock);

			// copy path information to the leave
			while (pathToSymmetricFollower.isEmpty() == false) {
				// CARE: the top of pathToSymmetricFollower should eventually be on top of pathTopDown
				pathTopDown.push(pathToSymmetricFollower.firstElement());
			}

			// delete symmetric follower because we duplicated this entry on the current node
			rebalanceTreeAfterDeletion(directorySectionHandler, pathTopDown, symFollower);
		}
	}

	private void rebalanceTreeAfterDeletion(DirectorySectionHandler directorySectionHandler, Stack<DirectoryBlock> pathTopDown,
			DirectoryEntryBlock fileNameBlock) {

		DirectoryBlock current = pathTopDown.pop();

		// check if deleting an element from a leaf node has brought it under the minimum size of 1
		if (current.getNodeLeft() != null) {
			// everything's fine;
			return;
		}

		throw new UnsupportedOperationException("Implement this");
		// TODO Auto-generated method stub

	}

	private DirectoryEntryBlock findAndDeleteSymmetricFollower(DirectorySectionHandler directorySectionHandler, long directoryBlockLocation,
			Stack<DirectoryBlock> pathToSymmetricFollower) throws VFSInvalidLocationExceptionException, VFSException, IOException {

		long currentLocation = directoryBlockLocation;
		while (true) {

			DirectoryBlock current = directorySectionHandler.loadDirectoryBlock(currentLocation);
			pathToSymmetricFollower.push(current);

			if ((currentLocation = current.getLinkRight()) != 0) {
				continue;
			} else if ((currentLocation = current.getLinkMiddle()) != 0) {
				continue;
			} else {
				DirectoryEntryBlock symmetricFollower = null;
				if (current.getNodeRight() != null) {
					symmetricFollower = current.getNodeRight();
					current.setNodeRight(null);
				} else {
					symmetricFollower = current.getNodeLeft();
					current.setNodeLeft(current.getNodeRight());
					current.setNodeRight(null);
				}

				directorySectionHandler.persistDirectoryBlock(current);
				return symmetricFollower;
			}
		}
	}

	private Stack<DirectoryBlock> findDirectoryBlockWithEntry(DirectorySectionHandler directorySectionHandler, DirectoryEntryBlock toFind) {

		// find the DirectoryEntryBlock to remove
		Stack<DirectoryBlock> pathToLeave = new Stack<DirectoryBlock>();

		DirectoryBlock current = rootBlock;

		try {

			while (true) {

				DirectoryEntryBlock node = current.getNodeLeft();
				if (node != null) {

					int cmpRes = toFind.compareTo(node);
					if (cmpRes < 0) {
						// travel down left branch
						pathToLeave.push(current);
						current = directorySectionHandler.loadDirectoryBlock(current.getLinkLeft());
					} else if (cmpRes == 0) {
						// found it
						pathToLeave.push(current);
						return pathToLeave;
					} else {

						node = current.getNodeRight();
						if (node == null) {
							// travel down middle branch
							pathToLeave.push(current);
							current = directorySectionHandler.loadDirectoryBlock(current.getLinkMiddle());
						} else {

							cmpRes = toFind.compareTo(node);
							if (cmpRes < 0) {
								// travel down middle branch
								pathToLeave.push(current);
								current = directorySectionHandler.loadDirectoryBlock(current.getLinkMiddle());

							} else if (cmpRes == 0) {
								// found it
								pathToLeave.push(current);
								return pathToLeave;
							} else {
								// travel down right branch
								pathToLeave.push(current);
								current = directorySectionHandler.loadDirectoryBlock(current.getLinkRight());
							}
						}
					}
				}
			}
		} catch (VFSInvalidLocationExceptionException ex) {
			throw new VFSRuntimeException("Could not delete Entry - File not found " + toFind.getFileName(), ex);
		} catch (VFSException e) {
			throw new VFSRuntimeException("", e);
		}
	}

	public String dumpTreeToString(DirectorySectionHandler directorySectionHandler) throws VFSException {
		StringBuffer buf = new StringBuffer();
		rootBlock.dumpShort(directorySectionHandler, buf, 0);
		return buf.toString();
	}
}
