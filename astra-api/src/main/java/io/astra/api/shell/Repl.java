package io.astra.api.shell;

import java.util.List;

/**
 * Read-eval-print loop for interactive agent control.
 */
public interface Repl {
    void start();
    void stop();
    void registerCommand(ShellCommand command);
    List<ShellCommand> getCommands();
}
