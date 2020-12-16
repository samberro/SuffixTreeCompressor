package com.samberro.trie;

public interface NodeFactory {
    Node obtain(byte val, int depth, Node parentNode);
}
