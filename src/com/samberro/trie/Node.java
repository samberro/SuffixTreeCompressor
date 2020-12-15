package com.samberro.trie;

import java.util.*;

import static com.samberro.trie.SuffixTrie.MAX_DISTANCE;

public class Node {
    public static int COUNT = 0;
    private static Stack<Node> POOL = new Stack<>();
    private static Node topHeap, bottomHeap;
    private static int POOL_MAX;

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
            System.out.println("Created Nodes: " + (COUNT / 1_000_000) + "mil, POOL: " + POOL.size());
    }

    public static Node obtain(byte val, int depth, Node parentNode) {
        if (POOL.isEmpty()) return new Node(val, depth, parentNode);
        if (POOL_MAX < POOL.size()) {
            POOL_MAX = POOL.size();
        }
        Node n = POOL.pop();
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
        bottomHeap.nextInHeap = this;
        previousInHeap = bottomHeap;
        nextInHeap = null;
        bottomHeap = this;

        if (lastSeenIndex - topHeap.lastSeenIndex > (MAX_DISTANCE)) dropTopLoop(lastSeenIndex - MAX_DISTANCE);
    }

    private void dropTopLoop(int dropIndex) {
        assertNotRecycled();
        int count = 0;
        while (topHeap.lastSeenIndex < dropIndex) {
            if (topHeap.parentNode != null) count += topHeap.parentNode.removeNode(topHeap);
            else throw new RuntimeException("removeFromHeap(topHeap);");
        }
//        System.out.println(" >> Dropped Nodes: " + count);
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

    public int removeNode(Node n) {
        int removed = 1;
//        nodes[n.val&0xFF] = null;
        nodes.remove(n.val);
        n.parentNode = null;

        removeFromHeap(n);
        Collection<Node> nodes = new ArrayList<>(n.getNodes());
        for (Node child : nodes) removed += n.removeNode(child);
        n.nodes.clear();
        n.recycle();
        return removed;
    }

    private static void removeFromHeap(Node n) {
        if (n == topHeap) {
            topHeap = n.nextInHeap;
            if (topHeap != null) topHeap.previousInHeap = null;
        } else if (n == bottomHeap) {
            bottomHeap = n.previousInHeap;
            if (bottomHeap != null) bottomHeap.nextInHeap = null;
        } else {
            n.previousInHeap.nextInHeap = n.nextInHeap;
            n.nextInHeap.previousInHeap = n.previousInHeap;
        }

        n.previousInHeap = null;
        n.nextInHeap = null;
    }

    private void recycle() {
        POOL.push(this);
        recycled = true;
    }

    public Node getParentNode() {
        return parentNode;
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
