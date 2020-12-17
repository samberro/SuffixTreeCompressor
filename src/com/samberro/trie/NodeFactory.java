package com.samberro.trie;

/**
 * Factory for Nodes.
 */
public interface NodeFactory {
    Node obtain(byte val, int depth, Node parentNode);
}
