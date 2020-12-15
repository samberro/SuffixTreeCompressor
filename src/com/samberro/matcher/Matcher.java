package com.samberro.matcher;

import com.samberro.Node;
import com.samberro.utils.Utils;

import java.util.*;

public class Matcher {
    public static final int MIN_MATCH = 3;

    private State state = State.IDLE;
    private MatchInfo currentMatch = new MatchInfo(-1, -1, null);
    private MatchInfo tempMatch = new MatchInfo(-1, -1, null);
    private MatchInfo readyMatch = null;
    private int maxLengthMatch = 0;
    private int noMatchCount = 0;
    private HashMap<Integer, Integer> matchCounts = new HashMap<>();

    public boolean hasMatch() {
        return readyMatch != null;
    }

    public MatchInfo getMatch() {
        if (hasMatch()) {
            tempMatch = readyMatch;
            readyMatch = null;
            return tempMatch;
        }
        return null;
    }

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
            if (next != null && next.getDepth() <= currentMatch.getMaxAllowedLength()) currentMatch.update(next);
            else if (currentMatch.getMatchLength() >= MIN_MATCH) transToReady();
            else transToNoMatch();
        }
    }

    private void startMatching(Node parent, Node next, int streamIndex) {
        if (next != null && parent.isRoot()) {
            state = State.MATCHING;
            currentMatch.startMatch(streamIndex, next);
        } else if (parent.isRoot()) noMatchCount++;
    }

    private void transToNoMatch() {
        state = State.IDLE;
    }

    private void transToReady() {
        state = State.IDLE;
        readyMatch = currentMatch;
        int matchLength = currentMatch.getMatchLength();
        Integer count = matchCounts.get(matchLength);
        count = count == null ? 1 : count + 1;
        matchCounts.put(matchLength, count);
        if (maxLengthMatch < matchLength) maxLengthMatch = matchLength;
        currentMatch = tempMatch;
    }

    public long getCompressedSize() {
        long noMatches = noMatchCount * 9L;
        long matches = 0L;
        for (Map.Entry<Integer, Integer> entry : matchCounts.entrySet()) {
            matches += entry.getValue() * 23L;
        }
        return (noMatches + matches) / 8L;
    }

    public void finish() {
        if (state == State.MATCHING && currentMatch.getMatchLength() >= MIN_MATCH) transToReady();
        else transToNoMatch();
    }

    public State getState() {
        return state;
    }

    public enum State {
        IDLE, MATCHING
    }

    @Override
    public String toString() {
        return "Matcher{" +
                "maxLengthMatch=" + maxLengthMatch +
                "compressed=" + Utils.humanReadableByteCountSI(getCompressedSize()) +
                '}';
    }
}
