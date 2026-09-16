package com.inscription.run;

import java.util.ArrayList;
import java.util.List;

/**
 * One point on the run's path. The whole path is a graph built upfront (like
 * a map generated before you start climbing) - each node knows which nodes
 * can follow it, and PathRunner asks the player to choose whenever there's
 * more than one option, or just moves on automatically when there's only one
 * (or the path ends, when there are none).
 */
public class PathNode {

    private final NodeContent content;
    private final List<PathNode> next = new ArrayList<>();

    public PathNode(NodeContent content) {
        this.content = content;
    }

    /** Connects this node to a possible next node - order matters, it's the order options are shown in. */
    public PathNode connectTo(PathNode node) {
        next.add(node);
        return node;
    }

    public List<PathNode> getNextOptions() {
        return List.copyOf(next);
    }

    public NodeContent getContent() {
        return content;
    }
}
