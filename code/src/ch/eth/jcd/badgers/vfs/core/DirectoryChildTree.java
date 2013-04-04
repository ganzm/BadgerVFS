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
	 * location of our upper most node of our tree
	 */
	private final long rootBlockLocation;

	public DirectoryChildTree(DirectoryBlock rootBlock) {
		this.rootBlockLocation = rootBlock.getLocation();
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

		DirectoryBlock rootBlock = directorySectionhandler.loadDirectoryBlock(rootBlockLocation);
		visitEntryBlock(directorySectionhandler, rootBlock, result);

		return result;
	}

	public long getRootBlockLocation() {
		return rootBlockLocation;
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
			bottomUpTreeInsertRightNodeEmpty(directorySectionhandler, newEntry, lastDirectoryBlock, directoryBlockToAttach, currentBlock);

		} else {
			// Otherwise the node is full, evenly split it into two nodes so:
			// A single median is chosen from among the leaf's elements and the new element.

			DirectoryEntryBlock[] sorted = sort(currentBlock.getNodeLeft(), currentBlock.getNodeRight(), newEntry);

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

	private void bottomUpTreeInsertRightNodeEmpty(DirectorySectionHandler directorySectionhandler, DirectoryEntryBlock newEntry,
			DirectoryBlock lastDirectoryBlock, DirectoryBlock directoryBlockToAttach, DirectoryBlock currentBlock) throws IOException {
		// right block is still empty
		// this node has only 2 sub nodes
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
	}

	private void insertAtRoot(DirectorySectionHandler directorySectionhandler, DirectoryEntryBlock newEntry, DirectoryBlock rootDirectoryBlock,
			DirectoryBlock directoryBlockToAttach) throws IOException {

		assert rootDirectoryBlock.getLocation() == rootBlockLocation;

		// since we don't want do swap our root block we need to copy values from our current
		// root block to the newly created one
		DirectoryBlock newBlock = directorySectionhandler.allocateNewDirectoryBlock();
		newBlock.copyValuesFrom(rootDirectoryBlock);

		rootDirectoryBlock.setNodeLeft(newEntry);
		rootDirectoryBlock.setNodeRight(null);

		if (newBlock.getNodeLeft().compareTo(directoryBlockToAttach.getNodeLeft()) < 0) {

			rootDirectoryBlock.setLinkLeft(newBlock.getLocation());
			rootDirectoryBlock.setLinkMiddle(directoryBlockToAttach.getLocation());
			rootDirectoryBlock.setLinkRight(0);
		} else {

			rootDirectoryBlock.setLinkLeft(directoryBlockToAttach.getLocation());
			rootDirectoryBlock.setLinkMiddle(newBlock.getLocation());
			rootDirectoryBlock.setLinkRight(0);
		}

		directorySectionhandler.persistDirectoryBlock(rootDirectoryBlock);
		directorySectionhandler.persistDirectoryBlock(newBlock);
	}

	private DirectoryEntryBlock[] sort(DirectoryEntryBlock nodeLeft, DirectoryEntryBlock nodeRight, DirectoryEntryBlock newEntry) {
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

		DirectoryBlock rootBlock = directorySectionhandler.loadDirectoryBlock(rootBlockLocation);
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
	 * 
	 * @returns the DirectoryEntry which was removed
	 */
	public DirectoryEntryBlock remove(DirectorySectionHandler directorySectionHandler, String fileName) throws VFSException, IOException {

		DirectoryEntryBlock deletedNode = null;

		// dummy block
		DirectoryEntryBlock fileNameBlock = new DirectoryEntryBlock(fileName);

		// search block where we delete an element
		Stack<DirectoryBlock> pathTopDown = findDirectoryBlockWithEntry(directorySectionHandler, fileNameBlock);

		DirectoryBlock currentDirectoryBlock = pathTopDown.peek();

		long leftSideLinkLocation = 0;
		boolean deleteLeftNode;
		if (currentDirectoryBlock.getNodeLeft().compareTo(fileNameBlock) == 0) {
			// we want to delete the left Entry of the currentDirectoryBlock
			leftSideLinkLocation = currentDirectoryBlock.getLinkLeft();
			deleteLeftNode = true;
		} else {
			// we want to delete the right Entry of the currentDirectoryBlock
			leftSideLinkLocation = currentDirectoryBlock.getLinkMiddle();
			deleteLeftNode = false;
		}

		if (leftSideLinkLocation == 0) {
			// we are trying to remove an entry from a leave

			// simply delete the entry
			if (deleteLeftNode) {
				deletedNode = currentDirectoryBlock.getNodeLeft();

				currentDirectoryBlock.setNodeLeft(currentDirectoryBlock.getNodeRight());
				currentDirectoryBlock.setNodeRight(null);
			} else {
				deletedNode = currentDirectoryBlock.getNodeRight();

				currentDirectoryBlock.setNodeRight(null);
			}
			directorySectionHandler.persistDirectoryBlock(currentDirectoryBlock);

			// rebalance the tree
			rebalanceTreeAfterDeletion(directorySectionHandler, pathTopDown);

		} else {
			// we are trying to remove an internal node of our tree

			Stack<DirectoryBlock> pathToSymmetricFollower = new Stack<>();
			DirectoryEntryBlock symFollower = findAndDeleteSymmetricFollower(directorySectionHandler, leftSideLinkLocation, pathToSymmetricFollower);

			// replace node to delete with symmetric follower
			if (deleteLeftNode) {
				deletedNode = currentDirectoryBlock.getNodeLeft();
				currentDirectoryBlock.setNodeLeft(symFollower);
			} else {
				deletedNode = currentDirectoryBlock.getNodeRight();
				currentDirectoryBlock.setNodeRight(symFollower);
			}
			directorySectionHandler.persistDirectoryBlock(currentDirectoryBlock);

			// copy path information to the leave
			while (!pathToSymmetricFollower.isEmpty()) {
				// CARE: the top of pathToSymmetricFollower should eventually be on top of pathTopDown
				DirectoryBlock removed = pathToSymmetricFollower.remove(0);
				pathTopDown.push(removed);
			}

			rebalanceTreeAfterDeletion(directorySectionHandler, pathTopDown);
		}

		return deletedNode;
	}

	private void rebalanceTreeAfterDeletion(DirectorySectionHandler directorySectionHandler, Stack<DirectoryBlock> pathTopDown) throws IOException,
			VFSInvalidLocationExceptionException, VFSException {

		DirectoryBlock current = pathTopDown.pop();

		// check if deleting an element from a leaf node has brought it under the minimum size of 1
		if (current.getNodeLeft() != null) {
			// everything's fine;
			return;
		}

		// check if this is the root node and the tree is now empty
		if (pathTopDown.isEmpty()) {
			// we are at the root here

			if (current.getLinkLeft() != 0) {
				// current node (rootBlock) is empty
				// but there is a link
				// promote linked node to be the new root
				assert current.getLocation() == rootBlockLocation;

				DirectoryBlock leftLowerFromRootBlock = directorySectionHandler.loadDirectoryBlock(current.getLinkLeft());
				current.copyValuesFrom(leftLowerFromRootBlock);

				directorySectionHandler.persistDirectoryBlock(current);
				directorySectionHandler.freeDirectoryBlock(leftLowerFromRootBlock);
			}

			return;
		}

		// check if we can solve the rebalancing problem by rotation
		DirectoryBlock parent = pathTopDown.peek();

		if (tryRebalanceByRotating(directorySectionHandler, current, parent)) {
			return;
		}

		if (parent.getLinkLeft() == current.getLocation()) {
			// current node is the left branch
			// merge with right sibling and pivot node from parent
			DirectoryBlock rightSibling = directorySectionHandler.loadDirectoryBlock(parent.getLinkMiddle());

			// move pivot one level down
			current.setNodeLeft(parent.getNodeLeft());
			current.setNodeRight(rightSibling.getNodeLeft());

			parent.setNodeLeft(parent.getNodeRight());
			parent.setNodeRight(null);
			parent.setLinkMiddle(parent.getLinkRight());
			parent.setLinkRight(0);

			current.setLinkMiddle(rightSibling.getLinkLeft());
			current.setLinkRight(rightSibling.getLinkMiddle());

			directorySectionHandler.persistDirectoryBlock(current);
			directorySectionHandler.persistDirectoryBlock(parent);
			directorySectionHandler.freeDirectoryBlock(rightSibling);
		} else if (parent.getLinkMiddle() == current.getLocation()) {
			// current node is the middle branch
			// merge with left sibling and pivot node from parent
			DirectoryBlock leftSibling = directorySectionHandler.loadDirectoryBlock(parent.getLinkLeft());

			// move pivot one level down
			leftSibling.setNodeRight(parent.getNodeLeft());
			leftSibling.setLinkRight(current.getLinkLeft());

			parent.setNodeLeft(parent.getNodeRight());
			parent.setNodeRight(null);
			parent.setLinkMiddle(parent.getLinkRight());
			parent.setLinkRight(0);

			directorySectionHandler.persistDirectoryBlock(leftSibling);
			directorySectionHandler.persistDirectoryBlock(parent);
			directorySectionHandler.freeDirectoryBlock(current);

		} else {
			// current node is the right branch
			// merge with left sibling and pivot node from parent
			DirectoryBlock leftSibling = directorySectionHandler.loadDirectoryBlock(parent.getLinkMiddle());

			leftSibling.setNodeRight(parent.getNodeRight());
			leftSibling.setLinkRight(current.getLinkLeft());

			parent.setNodeRight(null);
			parent.setLinkRight(0);

			directorySectionHandler.persistDirectoryBlock(leftSibling);
			directorySectionHandler.persistDirectoryBlock(parent);
			directorySectionHandler.freeDirectoryBlock(current);
		}

		rebalanceTreeAfterDeletion(directorySectionHandler, pathTopDown);

	}

	/**
	 * check if we can solve the balancing problem by rotation
	 * 
	 * @param directorySectionHandler
	 * @param current
	 * @param parent
	 * @return
	 * @throws VFSInvalidLocationExceptionException
	 * @throws VFSException
	 * @throws IOException
	 */
	private boolean tryRebalanceByRotating(DirectorySectionHandler directorySectionHandler, DirectoryBlock current, DirectoryBlock parent)
			throws VFSInvalidLocationExceptionException, VFSException, IOException {

		if (parent.getLinkLeft() == current.getLocation()) {
			// current node is the left branch
			DirectoryBlock rightSibling = directorySectionHandler.loadDirectoryBlock(parent.getLinkMiddle());

			if (rightSibling.getNodeRight() != null) {
				// do a rotation left

				current.setNodeLeft(parent.getNodeLeft());
				parent.setNodeLeft(rightSibling.getNodeLeft());
				rightSibling.setNodeLeft(rightSibling.getNodeRight());
				rightSibling.setNodeRight(null);

				current.setLinkMiddle(rightSibling.getLinkLeft());
				rightSibling.setLinkLeft(rightSibling.getLinkMiddle());
				rightSibling.setLinkMiddle(rightSibling.getLinkRight());
				rightSibling.setLinkRight(0);

				directorySectionHandler.persistDirectoryBlock(current);
				directorySectionHandler.persistDirectoryBlock(parent);
				directorySectionHandler.persistDirectoryBlock(rightSibling);

				return true;
			}
		} else if (parent.getLinkMiddle() == current.getLocation()) {
			// current node is the middle branch

			DirectoryBlock leftSibling = directorySectionHandler.loadDirectoryBlock(parent.getLinkLeft());

			if (leftSibling.getNodeRight() != null) {
				// left sibling above minimum load

				current.setNodeLeft(parent.getNodeLeft());
				parent.setNodeLeft(leftSibling.getNodeRight());
				leftSibling.setNodeRight(null);

				current.setLinkMiddle(current.getLinkLeft());
				current.setLinkLeft(leftSibling.getLinkRight());
				leftSibling.setLinkRight(0);

				directorySectionHandler.persistDirectoryBlock(current);
				directorySectionHandler.persistDirectoryBlock(parent);
				directorySectionHandler.persistDirectoryBlock(leftSibling);

				return true;
			}

			if (parent.getLinkRight() != 0) {
				DirectoryBlock rightSibling = directorySectionHandler.loadDirectoryBlock(parent.getLinkRight());
				if (rightSibling.getNodeRight() != null) {
					// left rotation

					current.setNodeLeft(parent.getNodeRight());
					parent.setNodeRight(rightSibling.getNodeLeft());
					rightSibling.setNodeLeft(rightSibling.getNodeRight());
					rightSibling.setNodeRight(null);

					current.setLinkMiddle(rightSibling.getLinkLeft());
					rightSibling.setLinkLeft(rightSibling.getLinkMiddle());
					rightSibling.setLinkMiddle(rightSibling.getLinkRight());
					rightSibling.setLinkRight(0);

					directorySectionHandler.persistDirectoryBlock(current);
					directorySectionHandler.persistDirectoryBlock(parent);
					directorySectionHandler.persistDirectoryBlock(rightSibling);

					return true;
				}
			}

		} else {
			// current node is the right branch
			DirectoryBlock leftSibling = directorySectionHandler.loadDirectoryBlock(parent.getLinkMiddle());

			if (leftSibling.getNodeRight() != null) {
				current.setNodeLeft(parent.getNodeRight());
				parent.setNodeRight(leftSibling.getNodeRight());
				leftSibling.setNodeRight(null);

				current.setLinkMiddle(current.getLinkLeft());
				current.setLinkLeft(leftSibling.getLinkRight());
				leftSibling.setLinkRight(0);

				directorySectionHandler.persistDirectoryBlock(current);
				directorySectionHandler.persistDirectoryBlock(parent);
				directorySectionHandler.persistDirectoryBlock(leftSibling);

				return true;
			}
		}

		return false;
	}

	private DirectoryEntryBlock findAndDeleteSymmetricFollower(DirectorySectionHandler directorySectionHandler, long directoryBlockLocation,
			Stack<DirectoryBlock> pathToSymmetricFollower) throws VFSInvalidLocationExceptionException, VFSException, IOException {

		long currentLocation = directoryBlockLocation;
		while (true) {

			DirectoryBlock current = directorySectionHandler.loadDirectoryBlock(currentLocation);
			pathToSymmetricFollower.push(current);

			if ((currentLocation = current.getLinkRight()) != 0) {
				// follow right path down

				continue;
			} else if (current.getNodeRight() != null) {

				DirectoryEntryBlock symmetricFollower = current.getNodeRight();
				current.setNodeRight(null);
				directorySectionHandler.persistDirectoryBlock(current);
				return symmetricFollower;
			} else if ((currentLocation = current.getLinkMiddle()) != 0) {
				// follow middle path down

				continue;

			} else {

				// take left node as symmetric follower
				DirectoryEntryBlock symmetricFollower = current.getNodeLeft();
				current.setNodeLeft(current.getNodeRight());
				current.setNodeRight(null);
				directorySectionHandler.persistDirectoryBlock(current);
				return symmetricFollower;
			}
		}
	}

	private Stack<DirectoryBlock> findDirectoryBlockWithEntry(DirectorySectionHandler directorySectionHandler, DirectoryEntryBlock toFind)
			throws VFSInvalidLocationExceptionException, VFSException {

		// find the DirectoryEntryBlock to remove
		Stack<DirectoryBlock> pathToLeave = new Stack<DirectoryBlock>();

		DirectoryBlock rootBlock = directorySectionHandler.loadDirectoryBlock(rootBlockLocation);
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
		DirectoryBlock rootBlock = directorySectionHandler.loadDirectoryBlock(rootBlockLocation);
		rootBlock.dumpShort(directorySectionHandler, buf, 0);
		return buf.toString();
	}

	public boolean performTreeSanityCheck(DirectorySectionHandler directorySectionHandler, StringBuffer debugInformation)
			throws VFSInvalidLocationExceptionException, VFSException {

		DirectoryBlock rootBlock = directorySectionHandler.loadDirectoryBlock(rootBlockLocation);
		if (rootBlock.getNodeLeft() == null && rootBlock.getNodeRight() == null) {
			// special case for root node
			debugInformation.append("Tree empty");
			return true;
		}

		try {

			int depth = performDirectoryBlockSanityCheck(directorySectionHandler, rootBlock, null, null, 0);
			debugInformation.append("Tree depth is " + depth + "\n");
			return true;
		} catch (VFSException exception) {
			debugInformation.append(exception.getMessage());
			return false;
		}
	}

	private int performDirectoryBlockSanityCheck(DirectorySectionHandler directorySectionHandler, DirectoryBlock block, DirectoryEntryBlock leftPivot,
			DirectoryEntryBlock rightPivot, int recursionDepth) throws VFSException {

		DirectoryEntryBlock leftNode = block.getNodeLeft();
		DirectoryEntryBlock rightNode = block.getNodeRight();

		if (leftNode == null) {
			throw new VFSException("Empty LeftNode in Block " + block.getLocation());
		}

		if (leftPivot != null) {
			if (leftPivot.compareTo(leftNode) >= 0) {
				throw new VFSException("NodeOrder Violation on Block " + block.getLocation() + " With left Parent Pivot [" + leftPivot.getFileName()
						+ "] and LeftNode" + leftNode.getFileName() + "]");
			}
		}

		if (rightPivot != null) {
			if (rightPivot.compareTo(leftNode) <= 0) {
				throw new VFSException("NodeOrder Violation on Block " + block.getLocation() + " With right Parent Pivot [" + rightPivot.getFileName()
						+ "] and LeftNode[" + leftNode.getFileName() + "]");
			}
		}

		if (rightNode != null) {
			if (leftNode.compareTo(block.getNodeRight()) >= 0) {
				throw new VFSException("NodeOrder Violation on Block " + block.getLocation() + " [" + leftNode.getFileName() + "] [" + rightNode.getFileName()
						+ "]");
			}
		}

		int depth = 0;
		// check left subblocks
		if (block.getLinkLeft() != 0) {
			DirectoryBlock subBlock = directorySectionHandler.loadDirectoryBlock(block.getLinkLeft());
			int newDepth = performDirectoryBlockSanityCheck(directorySectionHandler, subBlock, null, leftNode, recursionDepth + 1);
			depth = Math.max(depth, newDepth);
		}

		// check middle subblocks
		if (block.getLinkMiddle() != 0) {
			DirectoryBlock subBlock = directorySectionHandler.loadDirectoryBlock(block.getLinkMiddle());
			int newDepth = performDirectoryBlockSanityCheck(directorySectionHandler, subBlock, leftNode, rightNode, recursionDepth + 1);
			depth = Math.max(depth, newDepth);
		}

		// check right subblocks
		if (block.getLinkRight() != 0) {
			DirectoryBlock subBlock = directorySectionHandler.loadDirectoryBlock(block.getLinkRight());
			int newDepth = performDirectoryBlockSanityCheck(directorySectionHandler, subBlock, rightNode, null, recursionDepth + 1);
			depth = Math.max(depth, newDepth);
		}

		// check if this is a leave
		if (block.getLinkLeft() == 0 && block.getLinkMiddle() == 0 && block.getLinkRight() == 0) {
			// leave found
			return 0;
		}

		return depth;
	}

}
