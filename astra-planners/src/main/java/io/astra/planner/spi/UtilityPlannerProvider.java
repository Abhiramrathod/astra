package io.astra.planner.spi;

import io.astra.api.*;
import io.astra.api.config.AstraConfig;
import io.astra.planner.utility.UtilityPlannerImpl;
import java.util.List;
import java.util.Map;

public class UtilityPlannerProvider implements PlannerProvider {
    @Override
    public PlannerType type() { return PlannerType.UTILITY; }

    @Override
    public GoapPlanner create(AstraConfig config,
                              Map<String, CompoundTaskDef> compoundTasks,
                              List<ActionInfo> actions) {
        return new UtilityPlannerImpl(config);
    }
}
