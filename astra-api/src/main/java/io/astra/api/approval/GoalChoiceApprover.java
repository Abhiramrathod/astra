package io.astra.api.approval;

import io.astra.api.GoalInfo;
import java.util.List;

/**
 * Strategy interface for selecting a goal from a list of candidates.
 */
public interface GoalChoiceApprover {
    GoalInfo chooseGoal(List<GoalInfo> candidates, String query);
}
