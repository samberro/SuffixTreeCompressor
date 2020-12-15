package com.samberro.trie;

import java.util.*;

import static com.samberro.trie.SuffixTrie.MAX_DISTANCE;

public class Node {
    public static int COUNT = 0;
    private static Node heapTop, heapBottom, recycleTop;

    //    private Node[] nodes = new Node[256];
    private HashMap<Byte, Node> nodes = new HashMap<>();
    private byte val;
    private int depth;
    private int lastSeenIndex = -1;
    private Node nextSuffix = null;

    private Node previousInHeap;
    private Node nextInHeap;
    private Node parentNode;

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

        n.lastSeenIndex = -1;
        n.nextSuffix = null;
        n.previousInHeap = null;
        n.nextInHeap = null;
        return n;
    }

    private static void moveRecycledTop() {
        recycleTop = recycleTop.nextInHeap == heapTop ? null : recycleTop.nextInHeap;
    }

    private static boolean hasRecycled() {
        return recycleTop != null;
    }

    public int getLastSeenIndex() {
        return lastSeenIndex;
    }

    public void setLastSeenIndex(int lastSeenIndex) {
        this.lastSeenIndex = lastSeenIndex;
        moveToBottomOfHeap();
    }

    private void moveToBottomOfHeap() {
        if (heapBottom == this) return;

        // First node created. Initialize heap
        if (heapTop == null) {
            heapTop = this;
            heapBottom = this;
            return;
        }
        // Connect previous and next
        if (previousInHeap != null) previousInHeap.nextInHeap = nextInHeap;
        if (nextInHeap != null) nextInHeap.previousInHeap = previousInHeap;
        // Move heapTop and recycle pointers if we need to
        if (heapTop == this) heapTop = nextInHeap;

        previousInHeap = heapBottom;
        heapBottom.nextInHeap = this;
        heapBottom = this;
        nextInHeap = null;

        if (lastSeenIndex - heapTop.lastSeenIndex > (MAX_DISTANCE)) dropStale(lastSeenIndex - MAX_DISTANCE);
    }

    private void dropStale(int dropIndex) {
        while (heapTop.lastSeenIndex < dropIndex) {
            heapTop.parentNode.removeNode(heapTop);
            moveHeapTop();
        }
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

    public void removeNode(Node n) {
//        nodes[n.val&0xFF] = null;
        nodes.remove(n.val);
        n.parentNode = null;

        n.nodes.clear();
    }

    private static void moveHeapTop() {
        if (recycleTop == null) recycleTop = heapTop;
        heapTop = heapTop.nextInHeap;
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
