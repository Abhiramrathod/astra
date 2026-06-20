package io.astra.core.info;

import io.astra.api.GoalInfo;
import java.util.*;

public class DefaultGoalInfo implements GoalInfo {
    private final String name;
    private final Map<String, String> condition;

    public DefaultGoalInfo(String name, Map<String, String> condition) {
        this.name = name;
        this.condition = condition;
    }

    @Override
    public String getName() { return name; }
    @Override
    public Map<String, String> getCondition() { return condition; }

    @Override
    public String toString() {
        return "GoalInfo{name='" + name + "'}";
    }
}
