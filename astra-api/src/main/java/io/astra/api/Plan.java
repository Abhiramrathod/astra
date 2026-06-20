package io.astra.api;

import java.util.List;

public interface Plan {
    List<ActionInfo> getActions();
    double getTotalCost();
    boolean isExecutable();
    WorldState execute(WorldState initialState);
}
