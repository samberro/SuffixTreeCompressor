package com.samberro;

import java.util.*;

public class Node {
    public static int COUNT = 0;
    private static Stack<Node> POOL = new Stack<>();

    //    private Node[] nodes = new Node[256];
    private HashMap<Byte, Node> nodes = new HashMap<>();
    private byte val;
    private int depth;
    private int lastSeenIndex = -1;
    private Node nextSuffix = null;

    private Node(byte val, int depth) {
        this.val = val;
        this.depth = depth;
        COUNT++;
    }

    public static Node obtain(byte val, int depth) {
        if (POOL.isEmpty()) return new Node(val, depth);
        Node n = POOL.pop();
        n.val = val;
        n.depth = depth;

        for (Node child : n.getNodes()) POOL.push(child);
//        n.nodes.clear();
        n.lastSeenIndex = -1;
        n.nextSuffix = null;
        return n;
    }

    public int getLastSeenIndex() {
        return lastSeenIndex;
    }

    public void setLastSeenIndex(int lastSeenIndex) {
        this.lastSeenIndex = lastSeenIndex;
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
        nodes.put(b, node);
//        nodes[b&0xFF] = node;
    }

    @Override
    public String toString() {
        return depth == 0 ? "root" : "Node{" +
                "val=" + String.format("%02X", val) +
                ", depth=" + depth +
                ", lastSeenIndex=" + lastSeenIndex +
                ", nextSuffix=" + nextSuffix +
                '}';
    }

    public Collection<Node> getNodes() {
        return nodes.values();
//        return Arrays.asList(nodes);
    }

    public void removeNode(Node n) {
//        nodes[n.val&0xFF] = null;
        nodes.remove(n.val);
//        n.recycle();
    }

    private void recycle() {
        POOL.push(this);
    }
}
