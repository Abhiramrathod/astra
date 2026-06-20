package io.astra.planner.costbased;

import io.astra.api.*;
import java.util.*;

class SearchNode {
    final WorldState state;
    final SearchNode parent;
    final ActionInfo action;
    final double costSoFar;

    SearchNode(WorldState state, SearchNode parent, ActionInfo action, double costSoFar) {
        this.state = state;
        this.parent = parent;
        this.action = action;
        this.costSoFar = costSoFar;
    }
}
