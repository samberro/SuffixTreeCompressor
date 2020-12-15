package com.samberro.matcher;

import com.samberro.Node;

public class MatchInfo {
    private int originalIndex;
    private int destIndex;
    private Node matchNode;

    public MatchInfo(int originalIndex, int destIndex, Node matchNode) {
        this.originalIndex = originalIndex;
        this.destIndex = destIndex;
        this.matchNode = matchNode;
    }

    public void update(Node node) {
        matchNode = node;
        originalIndex = node == null ? -1 : (node.getLastSeenIndex() - node.getDepth() + 1);
    }

    public int getOriginalIndex() {
        return originalIndex;
    }

    public int getDestIndex() {
        return destIndex;
    }

    Node getMatchNode() {
        return matchNode;
    }

    public int getMatchLength() {
        return matchNode.getDepth();
    }

    public int getMaxAllowedLength() {
        // prevent run over
        return destIndex - originalIndex;
    }

    public void startMatch(int destIndex, Node node) {
        this.destIndex = destIndex;
        update(node);
    }
}
