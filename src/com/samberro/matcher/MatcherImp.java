package com.samberro.matcher;

import com.samberro.trie.Node;

public class MatcherImp implements Matcher {

    private State state = State.IDLE;
    private MatchInfo currentMatch = new MatchInfo(-1, -1, null);
    private MatchListener listener;

    public MatcherImp(MatchListener listener) {
        this.listener = listener;
    }

    @Override
    public void update(Node parent, Node next, int streamIndex) {
        switch (state) {
            case IDLE:
                startMatching(parent, next, streamIndex);
                break;
            case MATCHING:
                continueMatching(parent, next, streamIndex);
                break;
            default:
                break;
        }
    }

    private void continueMatching(Node parent, Node next, int streamIndex) {
        if (currentMatch.getMatchNode() == parent) {
            if (next != null &&
                    streamIndex - next.getLastIndex() <= 0xFFFF &&
                    next.getDepth() <= currentMatch.getMaxAllowedLength()) currentMatch.update(next);
            else if (currentMatch.getMatchLength() >= MIN_MATCH) deliverMatchReady();
            else deliverNoMatch();
        }
    }

    private void startMatching(Node parent, Node next, int streamIndex) {
        if (next != null && parent.isRoot()
                && streamIndex - next.getLastIndex() <= 0xFFFF) {
            state = State.MATCHING;
            currentMatch.startMatch(streamIndex, next);
        } else if (parent.isRoot()) listener.onMatchFailed(streamIndex, 1);
    }

    private void deliverNoMatch() {
        state = State.IDLE;
        listener.onMatchFailed(currentMatch.getDestIndex(), currentMatch.getMatchLength());
    }

    private void deliverMatchReady() {
        state = State.IDLE;
        listener.onMatchReady(currentMatch.getOriginalIndex(), currentMatch.getDestIndex(), currentMatch.getMatchLength());
        currentMatch.update(null);
    }

    @Override
    public void finish() {
        if (state == State.MATCHING) {
            if (currentMatch.getMatchLength() >= MIN_MATCH) deliverMatchReady();
            else deliverNoMatch();
        }
    }

    public enum State {
        IDLE, MATCHING
    }
}
