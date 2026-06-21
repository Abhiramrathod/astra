package io.astra.api;

import io.astra.api.config.AstraConfig;
import java.util.List;
import java.util.Map;

/** Service provider interface for creating {@link Planner} instances. */
public interface PlannerProvider {
    PlannerType type();
    Planner create(AstraConfig config,
                       Map<String, CompoundTaskDef> compoundTasks,
                       List<ActionInfo> actions);
}
