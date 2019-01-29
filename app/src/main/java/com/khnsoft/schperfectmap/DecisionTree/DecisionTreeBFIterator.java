
package com.khnsoft.schperfectmap.DecisionTree;

import java.util.*;


/**
 * This class implements a breadth-first search over nodes of a tree.
 * It does not allow to remove nodes.
 **/
public class DecisionTreeBFIterator
    implements Iterator {
    
    private LinkedList queue;
    
    
    /**
     * Returns an iterator over nodes of a tree.  The iteration is done
     * breadth-first.
     *
     * @param root The node where the search begins.
     **/
    public DecisionTreeBFIterator(Node root) {
	if (root == null)
	    throw new IllegalArgumentException("Invalid 'null' root");
	
	queue = new LinkedList();
	queue.add(root);
    }

    public boolean hasNext() {
	return queue.size() != 0;
    }

    public Object next() {
	Node node = (Node) queue.removeLast();
	
	if (node instanceof TestNode) {
	    TestNode testNode = (TestNode) node;
	    for (int i = 0; i < testNode.nbSons(); i++)
		queue.addFirst(testNode.son(i));
	}
	
	return node;
    }

    public void remove() {
	throw new UnsupportedOperationException("Cannot remove nodes");
    }
}
