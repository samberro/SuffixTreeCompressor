package com.samberro.matcher;

import com.samberro.trie.Node;

public interface Matcher {
    int MIN_MATCH = 3;

    void update(Node parent, Node next, int streamIndex);

    void finish();

    interface MatchListener {
        void onMatchReady(int originIndex, int destIndex, int length);

        void onMatchFailed(int destIndex, int length);
    }
}
