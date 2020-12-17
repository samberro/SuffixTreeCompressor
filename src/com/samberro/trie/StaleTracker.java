package com.samberro.trie;

/**
 * Tracks recently used (and by definition also least recently used) objects for recycling purposes
 * @param <T>
 */
public interface StaleTracker<T> {
    void touch(T t);
}
