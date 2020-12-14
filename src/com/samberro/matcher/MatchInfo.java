package com.samberro.matcher;

import com.samberro.Node;

public class MatchInfo {
    private int matchPos;
    private Node matchNode;

    public MatchInfo(int matchPos, Node matchNode) {
        this.matchPos = matchPos;
        this.matchNode = matchNode;
    }

    public void update(Node node) {
        matchPos = node == null ? -1 : (node.getLastSeenIndex() - node.getDepth());
        matchNode = node;
    }

    public int getMatchPos() {
        return matchPos;
    }


    Node getMatchNode() {
        return matchNode;
    }

    public int getMatchLength() {
        return matchNode.getDepth();
    }
}
