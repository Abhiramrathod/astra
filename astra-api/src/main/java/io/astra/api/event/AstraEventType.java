package io.astra.api.event;

/**
 * Standard event types emitted throughout the Astra lifecycle.
 */
public enum AstraEventType {
    PLAN_STARTED,
    PLAN_COMPLETED,
    PLAN_FAILED,
    ACTION_BEFORE,
    ACTION_AFTER,
    ACTION_FAILED,
    GOAL_SATISFIED,
    GOAL_UNSATISFIABLE,
    AGENT_REGISTERED,
    AGENT_REMOVED,
    AGENT_MESSAGE,
    SKILL_LOADED,
    SKILL_UNLOADED,
    SCHEDULED_TASK_TRIGGERED,
    VALIDATION_FAILED,
    POLICY_DENIED,
    MCP_TOOL_CALLED,
    STATE_SNAPSHOT,
    STATE_ROLLBACK
}
