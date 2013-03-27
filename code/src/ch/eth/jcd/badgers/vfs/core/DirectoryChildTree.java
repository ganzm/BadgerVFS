package ch.eth.jcd.badgers.vfs.core;

import java.io.IOException;
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

	public void insert(DirectorySectionHandler directorySectionhandle, DirectoryEntryBlock directoryEntryBlock) {

		// ----------------------------
		// All insertions start at a leaf node.
		// To insert a new element, search the tree to find the leaf node where the new element should be added.
		// ----------------------------

		// TODO
		/*
		 * If the node contains fewer than the maximum legal number of elements, then there is room for the new element. Insert the new element in the node,
		 * keeping the node's elements ordered. Otherwise the node is full, evenly split it into two nodes so: A single median is chosen from among the leaf's
		 * elements and the new element. Values less than the median are put in the new left node and values greater than the median are put in the new right
		 * node, with the median acting as a separation value. The separation value is inserted in the node's parent, which may cause it to be split, and so on.
		 * If the node has no parent (i.e., the node was the root), create a new root above this node (increasing the height of the tree). If the splitting goes
		 * all the way up to the root, it creates a new root with a single separator value and two children, which is why the lower bound on the size of
		 * internal nodes does not apply to the root. The maximum number of elements per node is U−1. When a node is split, one element moves to the parent, but
		 * one element is added. So, it must be possible to divide the maximum number U−1 of elements into two legal nodes. If this number is odd, then U=2L and
		 * one of the new nodes contains (U−2)/2 = L−1 elements, and hence is a legal node, and the other contains one more element, and hence it is legal too.
		 * If U−1 is even, then U=2L−1, so there are 2L−2 elements in the node. Half of this number is L−1, which is the minimum number of elements allowed per
		 * node. An improved algorithm (Mond & Raz 1985) supports a single pass down the tree from the root to the node where the insertion will take place,
		 * splitting any full nodes encountered on the way. This prevents the need to recall the parent nodes into memory, which may be expensive if the nodes
		 * are on secondary storage. However, to use this improved algorithm, we must be able to send one element to the parent and split the remaining U−2
		 * elements into two legal nodes, without adding a new element. This requires U = 2L rather than U = 2L−1, which accounts for why some textbooks impose
		 * this requirement in defining B-trees. // Insert the new element into that node with the following steps:
		 */
		// TODO Auto-generated method stub

	}
}
