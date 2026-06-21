package io.astra.planner.spi;

import io.astra.api.*;
import io.astra.api.config.AstraConfig;
import io.astra.planner.structural.StructuralPlanner;
import java.util.List;
import java.util.Map;

/** SPI provider for {@link io.astra.planner.structural.StructuralPlanner}. */
public class StructuralPlannerProvider implements PlannerProvider {
    @Override
    public PlannerType type() { return PlannerType.STRUCTURAL; }

    @Override
    public Planner create(AstraConfig config,
                          Map<String, CompoundTaskDef> compoundTasks,
                          List<ActionInfo> actions) {
        return new StructuralPlanner(config, compoundTasks, actions);
    }
}
