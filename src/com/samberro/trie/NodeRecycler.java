package com.samberro.trie;

import static com.samberro.trie.SuffixTrie.MAX_DISTANCE;

/**
 * Node factory and recycler class. It keeps track of the all the nodes
 * in a doubly linked list ordered from least used to most recent.
 * It also maintains a pointer to the next Node in the that can be recycled via {@link #recycleTop},
 * the top of the active nodes in the and the bottom of the nodes list. The pointers allows it to perform
 * update operations in O(1) time complexity
 */
public class NodeRecycler implements NodeFactory, StaleTracker<RecyclableNode> {
    private RecyclableNode activeTop, activeBottom, recycleTop;

    @Override
    public Node obtain(byte val, int depth, Node parentNode) {
        if (!hasRecycled()) {
            Stats.nodesCreated++;
            return new RecyclableNode(val, depth, parentNode, this);
        }
        Stats.nodesRecycled++;

        RecyclableNode n = recycleTop;
        moveRecycledTop();

        n.resetTo(val, depth, parentNode);
        return n;
    }

    private boolean hasRecycled() {
        return recycleTop != null;
    }

    private void moveRecycledTop() {
        recycleTop = recycleTop.nextInRecycleList == activeTop ? null : recycleTop.nextInRecycleList;
    }

    /**
     * Move the recently used node to the bottom of the list
     * @param node
     */
    private void moveToBottomOfActiveList(RecyclableNode node) {
        if (activeBottom == node) return;

        // First node created. Initialize heap
        if (activeTop == null) {
            activeTop = node;
            activeBottom = node;
            return;
        }
        // Connect previous and next
        if (node.previousInRecycleList != null) node.previousInRecycleList.nextInRecycleList = node.nextInRecycleList;
        if (node.nextInRecycleList != null) node.nextInRecycleList.previousInRecycleList = node.previousInRecycleList;
        // Move heapTop and recycle pointers if we need to
        if (activeTop == node) activeTop = node.nextInRecycleList;

        node.previousInRecycleList = activeBottom;
        activeBottom.nextInRecycleList = node;
        activeBottom = node;
        node.nextInRecycleList = null;

        // if we exceeded our sliding window of 0xFFFF (MAX_DISTANCE) then add shrink active list
        if (node.lastIndex - activeTop.lastIndex > MAX_DISTANCE) recycleStaleNodes(node.lastIndex - MAX_DISTANCE);
    }

    /**
     * Moves the active nodes pointer until we are back within the sliding window.
     * @param minIndex min index to put us in the sliding window
     */
    private void recycleStaleNodes(int minIndex) {
        //This loop executes in O(1) time complexity
        // and worst case is it loops SUFFIX_MAX_LENGTH=66 times
        while (activeTop.lastIndex < minIndex) {
            activeTop.removeNode();
            moveActivePointer();
        }
    }

    private void moveActivePointer() {
        if (recycleTop == null) recycleTop = activeTop;
        activeTop = activeTop.nextInRecycleList;
    }

    @Override
    public void touch(RecyclableNode node) {
        moveToBottomOfActiveList(node);
    }

    public static class Stats {
        public static int nodesCreated, nodesRecycled;

    }
}
