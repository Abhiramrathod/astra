package io.astra.planner.goap;

import io.astra.api.*;
import java.util.*;

class AStarNode {
    final WorldState state;
    final AStarNode parent;
    final ActionInfo action;
    final double costSoFar;

    AStarNode(WorldState state, AStarNode parent, ActionInfo action, double costSoFar) {
        this.state = state;
        this.parent = parent;
        this.action = action;
        this.costSoFar = costSoFar;
    }
}
