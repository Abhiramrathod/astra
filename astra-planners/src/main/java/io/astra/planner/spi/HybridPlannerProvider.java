package io.astra.planner.spi;

import io.astra.api.*;
import io.astra.api.config.AstraConfig;
import io.astra.planner.hybrid.HybridPlannerImpl;
import java.util.List;
import java.util.Map;

public class HybridPlannerProvider implements PlannerProvider {
    @Override
    public PlannerType type() { return PlannerType.HYBRID; }

    @Override
    public GoapPlanner create(AstraConfig config,
                              Map<String, CompoundTaskDef> compoundTasks,
                              List<ActionInfo> actions) {
        return new HybridPlannerImpl(config);
    }
}
