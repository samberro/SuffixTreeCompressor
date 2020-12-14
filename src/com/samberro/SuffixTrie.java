package com.samberro;

import com.samberro.matcher.Matcher;

public class SuffixTrie {
    public static final int MAX_SUFFIX_LENGTH = 0b11_1111 + 3;
    public static final int MAX_DISTANCE = 0xFFFF;

    private Node root = Node.obtain((byte) 0, 0, null);
    private Node current = root;
    private Matcher matcher;

    public SuffixTrie(Matcher matcher) {
        this.matcher = matcher;
    }

    public void insertByte(byte val, int streamIndex) {
        current = insertByte(current, val, streamIndex);
        if (!validateHavePathToRoot(current)) throw new RuntimeException("No path to root at: " + streamIndex);
    }

    private boolean validateHavePathToRoot(Node node) {
        if (node == null) return false;
        if (node == root) return true;
        return validateHavePathToRoot(node.getNextSuffixLink());
    }

    private Node insertByte(Node node, byte val, int streamIndex) {
        if (node == null) return root;

        Node next = node.nodeAt(val);
        matcher.update(node, next);

        if (next == null && node.getDepth() < MAX_SUFFIX_LENGTH) {
            next = Node.obtain(val, node.getDepth() + 1, node);
            node.addNode(val, next);
        }

        if (next != null) {
            next.setLastSeenIndex(streamIndex);
            next.setNextSuffixLink(insertByte(node.getNextSuffixLink(), val, streamIndex));
        } else {
            // reached the max suffix length, point to next suffix link
            next = insertByte(node.getNextSuffixLink(), val, streamIndex);
        }

        if (next.getNextSuffixLink() != node && next != node) node.setNextSuffixLink(null);

        return next;
    }


    public int find(byte[] arr) {
        int index = 0;
        Node found = find(root, arr, index);
        return found == null ? -1 : found.getLastSeenIndex() - arr.length + 1;
    }

    private Node find(Node node, byte[] arr, int index) {
        if(index >= arr.length || node == null) return node;
        return find(node.nodeAt(arr[index]), arr, ++index);
    }
}
