package com.samberro.trie;

public interface StaleTracker<T> {
    void touch(T t);
}
