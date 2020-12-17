package com.samberro.trie;

import java.util.*;

/**
 * Represents a node in a {@link SuffixTrie}. Each node in the Trie represents a suffix of the input.
 * This suffix can be formed by scanning back up to the root node. The suffix node's {@link #depth} represents the
 * length of the suffix and the {@link #lastIndex} represents the last index of the last character in the suffix.
 * The index of the first character in the suffix is {@link #lastIndex} - {@link #depth} + 1.
 */
public class Node {
    private HashMap<Byte, Node> nodes = new HashMap<>();
    protected byte val;
    protected int depth;
    protected int lastIndex = -1;
    protected Node nextSuffix = null;
    protected Node parentNode;

    Node(byte val, int depth, Node parentNode) {
        this.val = val;
        this.depth = depth;
        this.parentNode = parentNode;
    }

    /**
     * The last time this
     * @return
     */
    public int getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(int lastIndex) {
        this.lastIndex = lastIndex;
    }

    public Node nodeAt(byte b) {
        return nodes.get(b);
    }

    public int getDepth() {
        return depth;
    }

    public Node getNextSuffix() {
        return nextSuffix;
    }

    public void setNextSuffixLink(Node nextSuffix) {
        this.nextSuffix = nextSuffix;
    }

    public void addNode(byte b, Node node) {
//        if (nodeAt(b) != null) throw new RuntimeException("Replacing is not allowed");
        nodes.put(b, node);
    }

    public void removeNode() {
        if (parentNode != null) parentNode.nodes.remove(val);
        parentNode = null;
        nodes.clear();
    }

    @Override
    public String toString() {
        return depth == 0 ? "root" : "Node{" +
                "val=" + String.format("%02X", val) +
                ", depth=" + depth +
                ", lastSeenIndex=" + lastIndex +
                ", nextSuffix=" + nextSuffix +
                '}';
    }
}
