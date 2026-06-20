package io.astra.planner.spi;

import io.astra.api.*;
import io.astra.api.config.AstraConfig;
import io.astra.planner.goap.GoapPlannerImpl;
import java.util.List;
import java.util.Map;

public class GoapPlannerProvider implements PlannerProvider {
    @Override
    public PlannerType type() { return PlannerType.GOAP; }

    @Override
    public GoapPlanner create(AstraConfig config,
                              Map<String, CompoundTaskDef> compoundTasks,
                              List<ActionInfo> actions) {
        return new GoapPlannerImpl(config);
    }
}
