package io.astra.planner.spi;

import io.astra.api.*;
import io.astra.api.config.AstraConfig;
import io.astra.planner.utilitybased.UtilityBasedPlanner;
import java.util.List;
import java.util.Map;

public class UtilityBasedPlannerProvider implements PlannerProvider {
    @Override
    public PlannerType type() { return PlannerType.UTILITY_BASED; }

    @Override
    public Planner create(AstraConfig config,
                          Map<String, CompoundTaskDef> compoundTasks,
                          List<ActionInfo> actions) {
        return new UtilityBasedPlanner(config);
    }
}
