package com.samberro.trie;

import static com.samberro.trie.SuffixTrie.MAX_DISTANCE;

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
        recycleTop = recycleTop.nextInList == activeTop ? null : recycleTop.nextInList;
    }

    private void moveActiveToBottom(RecyclableNode node) {
        if (activeBottom == node) return;

        // First node created. Initialize heap
        if (activeTop == null) {
            activeTop = node;
            activeBottom = node;
            return;
        }
        // Connect previous and next
        if (node.previousInList != null) node.previousInList.nextInList = node.nextInList;
        if (node.nextInList != null) node.nextInList.previousInList = node.previousInList;
        // Move heapTop and recycle pointers if we need to
        if (activeTop == node) activeTop = node.nextInList;

        node.previousInList = activeBottom;
        activeBottom.nextInList = node;
        activeBottom = node;
        node.nextInList = null;

        if (node.lastIndex - activeTop.lastIndex > MAX_DISTANCE) recycleStaleNodes(node.lastIndex - MAX_DISTANCE);
    }

    private void recycleStaleNodes(int minIndex) {
        while (activeTop.lastIndex < minIndex) {
            activeTop.removeNode();
            moveHeapTop();
        }
    }

    private void moveHeapTop() {
        if (recycleTop == null) recycleTop = activeTop;
        activeTop = activeTop.nextInList;
    }

    @Override
    public void touch(RecyclableNode node) {
        moveActiveToBottom(node);
    }

    public static class Stats {
        public static int nodesCreated, nodesRecycled;

    }
}
