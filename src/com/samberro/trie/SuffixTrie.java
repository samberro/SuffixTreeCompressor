package com.samberro.trie;

public class SuffixTrie {
    public static final int MIN_MATCH = 3;
    public static final int MAX_SUFFIX_LENGTH = 0b11_1111 + MIN_MATCH;
    public static final int MAX_DISTANCE = 0xFFFF;

    private Node current;
    private final Node root;
    private final NodeFactory nodeFactory;

    public SuffixTrie() {
//        this.nodeFactory = Node::new;
        this.nodeFactory = new NodeRecycler();
        this.root = this.current = nodeFactory.obtain((byte) 0, 0, null);
    }

    private boolean validateHavePathToRoot(Node node) {
        if (node == null) return false;
        if (node == root) return true;
        return validateHavePathToRoot(node.getNextSuffix());
    }

    public void insertByte(byte val, int streamIndex) {
        current = insertByte(current, val, streamIndex);
//        if (!validateHavePathToRoot(current)) throw new RuntimeException("No path to root at: " + streamIndex);
    }

    private Node insertByte(Node node, byte val, int streamIndex) {
        if (node == null) return root;

        Node next = node.nodeAt(val);

        if (next == null && node.getDepth() < MAX_SUFFIX_LENGTH) {
            next = nodeFactory.obtain(val, node.getDepth() + 1, node);
            node.addNode(val, next);
        }

        if (next != null) {
            next.setLastIndex(streamIndex);
            next.setNextSuffixLink(insertByte(node.getNextSuffix(), val, streamIndex));
        } else {
            // reached the max suffix length, point to next suffix link
            next = insertByte(node.getNextSuffix(), val, streamIndex);
        }

        if (next.getNextSuffix() != node && next != node) node.setNextSuffixLink(null);

        return next;
    }


    public int find(byte[] arr) {
        Node found = find(root, arr, 0);
        return found == null ? -1 : found.getLastIndex() - arr.length + 1;
    }

    private Node find(Node node, byte[] arr, int index) {
        if(index >= arr.length || node == null) return node;
        return find(node.nodeAt(arr[index]), arr, ++index);
    }

    public Node findLongestPrefix(byte[] arr, int index) {
        return findLongestPrefix(root, arr, index);
    }

    private Node findLongestPrefix(Node node, byte[] arr, int index) {
        if(node == null || index >= arr.length) return node;
        Node next = node.nodeAt(arr[index]);
        if(next != null && index - next.getLastIndex() < 0xFFFF) {
            node = findLongestPrefix(next, arr, ++index);
        }
        return node != root ? node : null;
    }
}
