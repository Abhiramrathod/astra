package io.astra.planner.spi;

import io.astra.api.*;
import io.astra.api.config.AstraConfig;
import io.astra.planner.hybrid.HybridPlanner;
import java.util.List;
import java.util.Map;

/** SPI provider for {@link io.astra.planner.hybrid.HybridPlanner}. */
public class HybridPlannerProvider implements PlannerProvider {
    @Override
    public PlannerType type() { return PlannerType.HYBRID; }

    @Override
    public Planner create(AstraConfig config,
                          Map<String, CompoundTaskDef> compoundTasks,
                          List<ActionInfo> actions) {
        return new HybridPlanner(config);
    }
}
