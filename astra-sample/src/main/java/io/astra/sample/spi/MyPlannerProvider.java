package io.astra.sample.spi;

import io.astra.api.*;
import io.astra.api.config.AstraConfig;
import java.util.List;
import java.util.Map;

public class MyPlannerProvider implements PlannerProvider {
    @Override
    public PlannerType type() {
        return PlannerType.COST_BASED;
    }

    @Override
    public Planner create(AstraConfig config,
                          Map<String, CompoundTaskDef> compoundTasks,
                          List<ActionInfo> actions) {
        return new MyCustomPlanner();
    }
}
