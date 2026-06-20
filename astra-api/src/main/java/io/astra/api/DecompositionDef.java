package io.astra.api;

import java.util.*;

public class DecompositionDef {
    private final String name;
    private final String description;
    private final Map<String, String> preconditions;
    private final List<String> subtasks;

    public DecompositionDef(String name, Map<String, String> preconditions, List<String> subtasks) {
        this(name, "", preconditions, subtasks);
    }

    public DecompositionDef(String name, String description, Map<String, String> preconditions, List<String> subtasks) {
        this.name = name;
        this.description = description;
        this.preconditions = preconditions;
        this.subtasks = subtasks;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Map<String, String> getPreconditions() { return preconditions; }
    public List<String> getSubtasks() { return subtasks; }
}
