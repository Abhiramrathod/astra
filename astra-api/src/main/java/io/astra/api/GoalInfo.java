package io.astra.api;

import java.util.Map;

public interface GoalInfo {
    String getName();
    Map<String, String> getCondition();
    default String getDescription() { return ""; }
}
