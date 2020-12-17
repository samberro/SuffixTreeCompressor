package com.samberro.trie;

/**
 * The Trie holding all the possible suffixes of an input byte stream within a
 * {@link #MAX_DISTANCE} sliding window. Rules of this Trie (let's call it T):
 * 1. The suffix trie is built incrementally; i.e. T(i) = T(i-1) + byteStream[i].
 * 2. No suffix of T is at the same time a prefix of another suffix of T
 * 3. There is always a path from longest suffix from last insertion to root via suffix insertion links
 * 4. Suffix terminating nodes can be internal or leaf nodes
 * 5. Suffix nodes hold the value of the last seen index in the stream
 * 6. Suffixes can have a max length of {@link #MAX_SUFFIX_LENGTH}
 *
 * Example Trie given the input byte stream [A, B, C]:
 *       (root)-\
 *     A    B   C
 *     B    C__/
 *     C*__/
 *
 *  The '*' represents the longest suffix from last iteration. The links at the bottom lead back to root
 *  and allow us to insert the next byte quickly. Suffix insertion links always point to the next suffix whose
 *  {@link Node#depth} is 1 less than its node's {@link Node#depth}. i.e. a suffix of length 12 will have a suffix
 *  insertion link to the next suffix of length 11 until root is reached.
 *
 * Insertion:
 * Inserting a new byte adds a node to all suffixes whose length < {@link #MAX_SUFFIX_LENGTH}. This is done
 * by maintaining a pointer to the longest suffix from previous iteration (represented by it's leaf node). Each
 * suffix ending node also maintains a pointer to the next longest suffix via {@link Node#nextSuffix}. Since we cap the
 * each suffix length to {@link #MAX_SUFFIX_LENGTH}, the {@link Node#nextSuffix} links allow us to perform
 * insertions with constant time complexity as we will have a max of 66 links to insert at. Inserting at root
 * will create a new node if root does not have a node with the value already. The new node will have
 * a {@link Node#depth} of 1 and will point to root via its {@link Node#nextSuffix} and itself will be pointed to by
 * the previously inserted/updated node with {@link Node#depth} of 2. In case root
 * does have a node with the needed value, then that node's {@link Node#lastIndex} is updated to reflect the new index
 * and it will point to root.
 *
 * Suffix Index and proximity information:
 * By design the Trie discards previous encounters of a given suffix S when S is encountered again. This happens
 * organically by virtue of updating the visited node's {@link Node#lastIndex} value. The suffix index is calculated
 * from its node's {@link Node#depth} (suffix's length) and {@link Node#lastIndex} (suffix's terminating byte index).
 * 
 */
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

    /**
     * Inserts a new byte into the trie
     * @param val the new byte to insert
     * @param streamIndex the index of the byte in the stream
     */
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


    /**
     * General search utility. Returns the node representing longest matched prefix in the arr
     * @param arr the arr of bytes to search for
     * @return the node where the prefix arr[0, node.depth] is found
     */
    public int find(byte[] arr) {
        Node found = find(root, arr, 0);
        return found == null ? -1 : found.getLastIndex() - arr.length + 1;
    }

    private Node find(Node node, byte[] arr, int index) {
        if(index >= arr.length || node == null) return node;
        return find(node.nodeAt(arr[index]), arr, ++index);
    }

    /**
     * Similar to {@link #find(byte[])} but takes an index into the input array at which
     * to start the search and ensures the match is not more than {@link #MAX_DISTANCE} away.
     * This is used to find matches while building the trie as it takes the input context into
     * search considerations. Max length of 0b11_1111 + 3 is automatically accounted for by the
     * trie's insertion rules
     *
     * @param arr the input byte array
     * @param index the index into the input array of the first byte to search for
     * @return the node representing the found prefix
     */
    public Node findLongestPrefix(byte[] arr, int index) {
        return findLongestPrefix(root, arr, index);
    }

    private Node findLongestPrefix(Node node, byte[] arr, int index) {
        if(node == null || index >= arr.length) return node;
        Node next = node.nodeAt(arr[index]);
        if(next != null && index - next.getLastIndex() < MAX_DISTANCE) {
            node = findLongestPrefix(next, arr, ++index);
        }
        return node != root ? node : null;
    }
}
