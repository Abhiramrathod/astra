package io.astra.planner.spi;

import io.astra.api.*;
import io.astra.api.config.AstraConfig;
import io.astra.planner.htn.HtnPlannerImpl;
import java.util.List;
import java.util.Map;

public class HtnPlannerProvider implements PlannerProvider {
    @Override
    public PlannerType type() { return PlannerType.HTN; }

    @Override
    public GoapPlanner create(AstraConfig config,
                              Map<String, CompoundTaskDef> compoundTasks,
                              List<ActionInfo> actions) {
        return new HtnPlannerImpl(config, compoundTasks, actions);
    }
}
