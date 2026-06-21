package io.astra.api;

import java.util.List;

/** A sequence of actions to achieve a goal, with total cost and executability. */
public interface Plan {
    List<ActionInfo> getActions();
    double getTotalCost();
    boolean isExecutable();
    WorldState execute(WorldState initialState);
}
