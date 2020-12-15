package com.samberro.trie;

import java.util.*;

import static com.samberro.trie.SuffixTrie.MAX_DISTANCE;

public class Node {
    public static int COUNT = 0;
    private static Node topHeap, bottomHeap, recycleTop;

    //    private Node[] nodes = new Node[256];
    private HashMap<Byte, Node> nodes = new HashMap<>();
    private byte val;
    private int depth;
    private int lastSeenIndex = -1;
    private Node nextSuffix = null;

    private Node previousInHeap;
    private Node nextInHeap;
    private Node parentNode;

    boolean recycled = false;

    private Node(byte val, int depth, Node parentNode) {
        this.val = val;
        this.depth = depth;
        this.parentNode = parentNode;
        COUNT++;
        if (COUNT % 1_000_000 == 0)
            System.out.println("Created Nodes: " + (COUNT / 1_000_000) + "mil");
    }

    public static Node obtain(byte val, int depth, Node parentNode) {
        if (!hasRecycled()) return new Node(val, depth, parentNode);

        Node n = recycleTop;
        moveRecycledTop();
        n.val = val;
        n.depth = depth;
        n.parentNode = parentNode;

        n.recycled = false;
        n.lastSeenIndex = -1;
        n.nextSuffix = null;
        n.previousInHeap = null;
        n.nextInHeap = null;
        return n;
    }

    private static void moveRecycledTop() {
        recycleTop = recycleTop.nextInHeap == topHeap ? null : recycleTop.nextInHeap;
    }

    private static boolean hasRecycled() {
        return recycleTop != null;
    }

    public int getLastSeenIndex() {
        assertNotRecycled();
        return lastSeenIndex;
    }

    public void setLastSeenIndex(int lastSeenIndex) {
        assertNotRecycled();
        this.lastSeenIndex = lastSeenIndex;
        moveToBottomOfHeap();
    }

    private void moveToBottomOfHeap() {
        assertNotRecycled();

        if (bottomHeap == this) return;
        if (bottomHeap == null) {
            bottomHeap = this;
            topHeap = this;
            return;
        }

        if (previousInHeap != null) previousInHeap.nextInHeap = nextInHeap;
        if (nextInHeap != null) nextInHeap.previousInHeap = previousInHeap;
        if (topHeap == this) topHeap = nextInHeap;
        if (recycleTop == this) moveRecycledTop();

        bottomHeap.nextInHeap = this;
        previousInHeap = bottomHeap;
        nextInHeap = null;
        bottomHeap = this;

        if (lastSeenIndex - topHeap.lastSeenIndex > (MAX_DISTANCE)) dropStale(lastSeenIndex - MAX_DISTANCE);
    }

    private void dropStale(int dropIndex) {
        assertNotRecycled();
        while (topHeap.lastSeenIndex < dropIndex) topHeap.parentNode.removeNode(topHeap);
    }

    public Node nodeAt(byte b) {
        assertNotRecycled();
//        return nodes[b&0xFF];
        return nodes.get(b);
    }

    public int getDepth() {
        assertNotRecycled();
        return depth;
    }

    public Node getNextSuffixLink() {
        assertNotRecycled();
        return nextSuffix;
    }

    public void setNextSuffixLink(Node nextSuffix) {
        assertNotRecycled();
        this.nextSuffix = nextSuffix;
    }

    public void addNode(byte b, Node node) {
        assertNotRecycled();
        if (nodeAt(b) != null) throw new RuntimeException("Replacing is not allowed");
        nodes.put(b, node);
//        nodes[b&0xFF] = node;
    }

    public Collection<Node> getNodes() {
        assertNotRecycled();
        return nodes.values();
//        return Arrays.asList(nodes);
    }

    public void removeNode(Node n) {
//        nodes[n.val&0xFF] = null;
        nodes.remove(n.val);
        n.parentNode = null;

        removeFromHeap(n);
        n.nodes.clear();
        n.recycle();
    }

    private static void removeFromHeap(Node n) {
        if (n == topHeap) {
            topHeap = n.nextInHeap;
        } else if (n == bottomHeap) {
            bottomHeap = n.previousInHeap;
            if (bottomHeap != null) bottomHeap.nextInHeap = null;
            throw new RuntimeException("bottom");
        } else {
            throw new RuntimeException("Middle");
//            n.previousInHeap.nextInHeap = n.nextInHeap;
//            n.nextInHeap.previousInHeap = n.previousInHeap;
        }

        if (recycleTop == null) recycleTop = n;
    }

    private void recycle() {
        recycled = true;
    }

    private void assertNotRecycled() {
        if (recycled) throw new RuntimeException("Recycled");
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


    public boolean isRoot() {
        return depth == 0;
    }
}
