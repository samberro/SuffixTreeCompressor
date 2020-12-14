package com.samberro.matcher;

import com.samberro.Node;
import com.samberro.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class Matcher {
    private static final int MIN_MATCH = 3;

    private State state = State.NO_MATCH;
    private MatchInfo currentMatch = new MatchInfo(-1, null);
    private int maxLengthMatch = 0;
    private int noMatchCount = 0;
    private HashMap<Integer, Integer> matchCounts = new HashMap<>();

    public State getState() {
        return state;
    }

    public MatchInfo getLatestMatch() {
        state = State.NO_MATCH;
        return currentMatch;
    }

    public void update(Node parent, Node next) {
        switch (state) {
            case NO_MATCH:
                startMatching(parent, next);
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
            if (next != null) currentMatch.update(next);
            else if (currentMatch.getMatchLength() >= MIN_MATCH) transToReady();
            else transToNoMatch();
        }
    }

    private void startMatching(Node parent, Node next) {
        if (next != null && parent.isRoot()) {
            state = State.MATCHING;
            currentMatch.update(next);
        } else if (parent.isRoot()) noMatchCount++;
    }

    private void transToNoMatch() {
        state = State.NO_MATCH;
    }

    private void transToReady() {
        state = State.READY;
        int matchLength = currentMatch.getMatchLength();
        Integer count = matchCounts.get(matchLength);
        count = count == null ? 1 : count + 1;
        matchCounts.put(matchLength, count);
        if (maxLengthMatch < matchLength) maxLengthMatch = matchLength;
    }

    public long getCompressedSize() {
        long noMatches = noMatchCount * 9L;
        long matches = 0L;
        for (Map.Entry<Integer, Integer> entry : matchCounts.entrySet()) {
            matches += entry.getValue() * 23L;
        }
        return (noMatches + matches) / 8L;
    }

    public enum State {
        NO_MATCH, MATCHING, READY
    }

    @Override
    public String toString() {
        return "Matcher{" +
                "maxLengthMatch=" + maxLengthMatch +
                "compressed=" + Utils.humanReadableByteCountSI(getCompressedSize()) +
                '}';
    }
}
