package com.samberro.trie;

/**
 * A node that can be tracked and recycled. Tells the {@link StaleTracker} when it is visited
 */
public class RecyclableNode extends Node {

    protected RecyclableNode previousInRecycleList;
    protected RecyclableNode nextInRecycleList;

    private StaleTracker<RecyclableNode> tracker;

    public RecyclableNode(byte val, int depth, Node parentNode,
                          StaleTracker<RecyclableNode> tracker) {
        super(val, depth, parentNode);
        this.tracker = tracker;
    }

    public void resetTo(byte val, int depth, Node parentNode) {
        this.val = val;
        this.depth = depth;
        this.parentNode = parentNode;
        this.lastIndex = -1;
        this.nextSuffix = null;
        this.previousInRecycleList = null;
        this.nextInRecycleList = null;
    }

    @Override
    public void setLastIndex(int lastIndex) {
        super.setLastIndex(lastIndex);
        tracker.touch(this);
    }
}
