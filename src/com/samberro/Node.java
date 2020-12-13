package com.samberro;

import java.util.Arrays;

public class Node {
    private Node[] nodes = new Node[256];
    private byte val;
    private int depth;
    private int lastSeenIndex = -1;
    private Node nextSuffix = null;

    public Node(byte val, int depth) {
        this.val = val;
        this.depth = depth;
    }

    public int getLastSeenIndex() {
        return lastSeenIndex;
    }

    public void setLastSeenIndex(int lastSeenIndex) {
        this.lastSeenIndex = lastSeenIndex;
    }

    public Node nodeAt(byte b) {
        int index = ((int)b) & 0xFF;
        return nodes[index];
    }

    public byte getVal() {
        return val;
    }

    public int getDepth() {
        return depth;
    }

    public Node getNextSuffixLink() {
        return nextSuffix;
    }

    public void setNextSuffixLink(Node nextSuffix) {
        this.nextSuffix = nextSuffix;
    }

    public void addNode(byte b, Node node) {
        if(nodeAt(b) != null) throw new RuntimeException("Replacing is not allowed");
        nodes[((int)b) & 0xFF] = node;
    }

    @Override
    public String toString() {
        return depth == 0 ? "root" : "Node{" +
                "val=" + String.format("%02X",val) +
                ", depth=" + depth +
                ", lastSeenIndex=" + lastSeenIndex +
                ", nextSuffix=" + nextSuffix +
                '}';
    }
}
