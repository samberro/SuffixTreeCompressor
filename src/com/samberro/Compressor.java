package com.samberro;

import com.samberro.utils.Match;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Compressor {
    private static final int MIN_MATCH = 3;
    private static final int MAX_DEPTH = 0b11_1111 + 3;

    private Node root = Node.obtain((byte) 0, 0);
    private Node current = root;

    public void insertByte(byte val, int streamIndex) {
        current = insertByte(current, val, streamIndex);
        if (!validateHavePathToRoot(current)) throw new RuntimeException("No path to root at: " + streamIndex);
    }

    public void cull(int index) {
        System.out.println("Culling @ " + index);
        int min = index - 0xFFFF;
        cull(root, min);
    }

    private void cull(Node node, int min) {
        ArrayList<Node> nodes = new ArrayList<>(node.getNodes());
        for (Node n : nodes) {
            if (n.getLastSeenIndex() < min) node.removeNode(n);
            else cull(n, min);
        }
    }

    private boolean validateHavePathToRoot(Node node) {
        if (node == null) return false;
        if (node == root) return true;
        return validateHavePathToRoot(node.getNextSuffixLink());
    }

    private Node insertByte(Node node, byte val, int streamIndex) {
        if (node == null) return root;

        Node next = node.nodeAt(val);

        if(next == null && node.getDepth() < MAX_DEPTH) {
            next = Node.obtain(val, node.getDepth() + 1);
            node.addNode(val, next);
        }

        if(next != null) {
            next.setLastSeenIndex(streamIndex);
            next.setNextSuffixLink(insertByte(node.getNextSuffixLink(), val, streamIndex));
        } else {
            // node.getDepth() >= MAX_DEPTH
            // reached the max suffix length
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

    public Match insertSuffix(byte[] bytes, int startIndex, int posInByteStream) {
        int matchLength = 0;
        int lastMatchPos = -1;
        int insertionPos = posInByteStream;
        Match match = null;
        Node current = root;
        for(int i = startIndex; i < bytes.length; i++) {
            byte b = bytes[i];
            current = current.nodeAt(b);
            if(current.getLastSeenIndex() >= 0) {
                lastMatchPos = current.getLastSeenIndex();
                matchLength++;
            }
            current.setLastSeenIndex(insertionPos++);
        }

        if(matchLength >= MIN_MATCH)
            match = Match.obtain(
                startIndex, matchLength, posInByteStream - (lastMatchPos - matchLength + 1));

        return match;
    }



}
