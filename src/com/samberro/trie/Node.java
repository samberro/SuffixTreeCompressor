package com.samberro.trie;

import java.util.*;

public class Node {
    //    private Node[] nodes = new Node[256];
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


    public int getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(int lastIndex) {
        this.lastIndex = lastIndex;
    }

    public Node nodeAt(byte b) {
//        return nodes[b&0xFF];
        return nodes.get(b);
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
        if (nodeAt(b) != null) throw new RuntimeException("Replacing is not allowed");
//        nodes[b&0xFF] = node;
        nodes.put(b, node);
    }

    public boolean isRoot() {
        return depth == 0;
    }

    public void removeNode() {
        if (parentNode != null) {
//        parentNode.nodes[n.val&0xFF] = null;
            parentNode.nodes.remove(val);
        }
        nodes.clear();
        parentNode = null;
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
