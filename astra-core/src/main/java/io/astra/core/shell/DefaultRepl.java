package io.astra.core.shell;

import io.astra.api.Astra;
import io.astra.api.WorldState;
import io.astra.api.WorldStates;
import io.astra.api.result.ExecutionResult;
import io.astra.api.shell.Repl;
import io.astra.api.shell.ShellCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Interactive read-eval-print loop for Astra that accepts built-in commands
 * (help, exec, goals, exit) and dispatches them against an {@link Astra} instance.
 */
public class DefaultRepl implements Repl {
    private static final Logger log = LoggerFactory.getLogger(DefaultRepl.class);
    private final Astra astra;
    private final List<ShellCommand> commands = new CopyOnWriteArrayList<>();
    private volatile boolean running;

    public DefaultRepl(Astra astra) {
        this.astra = astra;
        registerCommand(new HelpCommand());
        registerCommand(new ExecuteCommand());
        registerCommand(new GoalsCommand());
        registerCommand(new ExitCommand());
    }

    @Override
    public void start() {
        running = true;
        System.out.println("Astra REPL started. Type 'help' for commands, 'exit' to quit.");
        try (Scanner scanner = new Scanner(System.in)) {
            while (running && scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+", 2);
                String cmdName = parts[0];
                String[] args = parts.length > 1 ? parts[1].split("\\s+") : new String[0];
                boolean handled = false;
                for (ShellCommand cmd : commands) {
                    if (cmd.getName().equals(cmdName)) {
                        try {
                            String result = cmd.execute(args);
                            System.out.println(result);
                        } catch (Exception e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                        handled = true;
                        break;
                    }
                }
                if (!handled) {
                    System.out.println("Unknown command: " + cmdName + ". Type 'help'.");
                }
            }
        }
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public void registerCommand(ShellCommand command) {
        commands.add(command);
    }

    @Override
    public List<ShellCommand> getCommands() {
        return List.copyOf(commands);
    }

    private class HelpCommand implements ShellCommand {
        @Override public String getName() { return "help"; }
        @Override public String getDescription() { return "List all commands"; }
        @Override public String execute(String[] args) {
            StringBuilder sb = new StringBuilder("Available commands:\n");
            for (ShellCommand cmd : commands) {
                sb.append("  ").append(cmd.getName()).append(" - ").append(cmd.getDescription()).append("\n");
            }
            return sb.toString();
        }
    }

    private class ExecuteCommand implements ShellCommand {
        @Override public String getName() { return "exec"; }
        @Override public String getDescription() { return "Execute a goal: exec <goalName> [fact1=val1 fact2=val2 ...]"; }
        @Override public String execute(String[] args) {
            if (args.length < 1) return "Usage: exec <goalName> [facts...]";
            String goalName = args[0];
            Map<String, String> factMap = new LinkedHashMap<>();
            for (int i = 1; i < args.length; i++) {
                String[] kv = args[i].split("=", 2);
                if (kv.length == 2) factMap.put(kv[0], kv[1]);
            }
            WorldState state = factMap.isEmpty() ? WorldStates.empty() : WorldStates.of(factMap);
            ExecutionResult result = astra.executeWithResult(goalName, state);
            if (result.isSuccess()) {
                return "SUCCESS: " + result.getFinalState();
            }
            return "FAILURE: " + result.getErrorMessage();
        }
    }

    private class GoalsCommand implements ShellCommand {
        @Override public String getName() { return "goals"; }
        @Override public String getDescription() { return "List registered goals"; }
        @Override public String execute(String[] args) {
            return "Use agent introspection to list goals (not yet available in REPL)";
        }
    }

    private class ExitCommand implements ShellCommand {
        @Override public String getName() { return "exit"; }
        @Override public String getDescription() { return "Exit the REPL"; }
        @Override public String execute(String[] args) {
            stop();
            return "Goodbye!";
        }
    }
}
