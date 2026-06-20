package io.astra.core.info;

import io.astra.api.ActionInfo;
import java.util.*;

public class DefaultActionInfo implements ActionInfo {
    private final String name;
    private final String description;
    private final Map<String, String> preconditions;
    private final Map<String, String> effects;
    private final float cost;
    private final Runnable executor;
    private final float utility;
    private final String utilityMethod;

    public DefaultActionInfo(String name, String description, Map<String, String> preconditions,
                             Map<String, String> effects, float cost, Runnable executor,
                             float utility, String utilityMethod) {
        this.name = name;
        this.description = description;
        this.preconditions = preconditions;
        this.effects = effects;
        this.cost = cost;
        this.executor = executor;
        this.utility = utility;
        this.utilityMethod = utilityMethod;
    }

    public DefaultActionInfo(String name, Map<String, String> preconditions,
                             Map<String, String> effects, float cost, Runnable executor,
                             float utility, String utilityMethod) {
        this(name, "", preconditions, effects, cost, executor, utility, utilityMethod);
    }

    public DefaultActionInfo(String name, Map<String, String> preconditions,
                             Map<String, String> effects, float cost, Runnable executor) {
        this(name, "", preconditions, effects, cost, executor, 0.5f, "");
    }

    @Override
    public String getName() { return name; }
    @Override
    public String getDescription() { return description; }
    @Override
    public Map<String, String> getPreconditions() { return preconditions; }
    @Override
    public Map<String, String> getEffects() { return effects; }
    @Override
    public float getCost() { return cost; }
    @Override
    public Runnable getExecutor() { return executor; }
    @Override
    public float getUtility() { return utility; }
    @Override
    public String getUtilityMethod() { return utilityMethod; }

    @Override
    public String toString() {
        String desc = description.isEmpty() ? "" : " (" + description + ")";
        return "ActionInfo{name='" + name + desc + "', utility=" + utility + "}";
    }
}
