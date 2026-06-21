package io.astra.api;

import java.util.Map;

/** Provides metadata for a registered goal. */
public interface GoalInfo {
    String getName();
    Map<String, String> getCondition();
    default String getDescription() { return ""; }
}
