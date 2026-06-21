package io.astra.core.approval;

import io.astra.api.GoalInfo;
import io.astra.api.approval.GoalChoiceApprover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Console;
import java.util.List;
import java.util.Scanner;

/**
 * Console-based {@link GoalChoiceApprover} that prompts the user to select
 * from multiple matching goals via stdin.
 */
public class ConsoleGoalChoiceApprover implements GoalChoiceApprover {
    private static final Logger log = LoggerFactory.getLogger(ConsoleGoalChoiceApprover.class);
    private final Scanner scanner;

    public ConsoleGoalChoiceApprover() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public GoalInfo chooseGoal(List<GoalInfo> candidates, String query) {
        if (candidates.isEmpty()) throw new IllegalArgumentException("No candidates");
        if (candidates.size() == 1) return candidates.get(0);

        log.info("Multiple goals match query '{}'. Please choose:", query);
        for (int i = 0; i < candidates.size(); i++) {
            GoalInfo g = candidates.get(i);
            System.out.printf("  [%d] %s (%s)%n", i + 1, g.getName(), g.getDescription());
        }
        System.out.print("Enter choice (1-" + candidates.size() + "): ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid choice, using first candidate");
            return candidates.get(0);
        }
        if (choice < 1 || choice > candidates.size()) {
            log.warn("Choice out of range, using first candidate");
            return candidates.get(0);
        }
        return candidates.get(choice - 1);
    }
}
