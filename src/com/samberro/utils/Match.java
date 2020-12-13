package com.samberro.utils;

import java.util.Stack;

public class Match {
    private static Stack<Match> pool = new Stack<>();

    private int matchIndex;
    private int relativePos;
    private int length;

    private Match(int matchIndex, int length, int relativePos) {
        this.matchIndex = matchIndex;
        this.relativePos = relativePos;
        this.length = length;
    }

    public int getMatchIndex() {
        return matchIndex;
    }

    public void setMatchIndex(int matchIndex) {
        this.matchIndex = matchIndex;
    }

    public int getRelativePos() {
        return relativePos;
    }

    public void setRelativePos(int relativePos) {
        this.relativePos = relativePos;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void recycle() {
        Match.recycle(this);
    }

    public static Match obtain(int matchIndex, int length, int pos) {
        Match match;
        if(pool.isEmpty()) match = new Match(matchIndex, length, pos);
        else {
            match = pool.pop();
            match.setRelativePos(pos);
            match.setLength(length);
            match.setMatchIndex(matchIndex);
        }
        return match;
    }

    public static void recycle(Match m) {
        pool.push(m);
    }
}
