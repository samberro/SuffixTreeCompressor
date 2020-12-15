package com.samberro.matcher;

import com.samberro.Node;

public class Matcher {
    public static final int MIN_MATCH = 3;

    private State state = State.IDLE;
    private MatchInfo currentMatch = new MatchInfo(-1, -1, null);
    private MatchListener listener;

    public Matcher(MatchListener listener) {
        this.listener = listener;
    }

    public void update(Node parent, Node next, int streamIndex) {
        switch (state) {
            case IDLE:
                startMatching(parent, next, streamIndex);
                break;
            case MATCHING:
                continueMatching(parent, next);
                break;
            default:
                break;
        }
    }

    private void continueMatching(Node parent, Node next) {
        if (currentMatch.getMatchNode() == parent) {
            if (next != null && next.getDepth() <= currentMatch.getMaxAllowedLength()) currentMatch.update(next);
            else if (currentMatch.getMatchLength() >= MIN_MATCH) deliverMatchReady();
            else deliverNoMatch();
        }
    }

    private void startMatching(Node parent, Node next, int streamIndex) {
        if (next != null && parent.isRoot()) {
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

    public void finish() {
        if (state == State.MATCHING) {
            if (currentMatch.getMatchLength() >= MIN_MATCH) deliverMatchReady();
            else deliverNoMatch();
        }
    }

    public enum State {
        IDLE, MATCHING
    }

    public interface MatchListener {
        void onMatchReady(int originIndex, int destIndex, int length);

        void onMatchFailed(int destIndex, int length);
    }
}
