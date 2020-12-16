package com.samberro.trie;

public class RecyclableNode extends Node {

    protected RecyclableNode previousInList;
    protected RecyclableNode nextInList;

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
        this.previousInList = null;
        this.nextInList = null;
    }

    @Override
    public void setLastIndex(int lastIndex) {
        super.setLastIndex(lastIndex);
        tracker.touch(this);
    }
}
