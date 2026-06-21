package io.astra.planner.spi;

import io.astra.api.*;
import io.astra.api.config.AstraConfig;
import io.astra.planner.costbased.CostBasedPlanner;
import java.util.List;
import java.util.Map;

/** SPI provider for {@link io.astra.planner.costbased.CostBasedPlanner}. */
public class CostBasedPlannerProvider implements PlannerProvider {
    @Override
    public PlannerType type() { return PlannerType.COST_BASED; }

    @Override
    public Planner create(AstraConfig config,
                          Map<String, CompoundTaskDef> compoundTasks,
                          List<ActionInfo> actions) {
        return new CostBasedPlanner(config);
    }
}
