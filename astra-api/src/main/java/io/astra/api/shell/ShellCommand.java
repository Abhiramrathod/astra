package io.astra.api.shell;

/**
 * A command registered with the {@link Repl} shell.
 */
public interface ShellCommand {
    String getName();
    String getDescription();
    String execute(String[] args);
}
