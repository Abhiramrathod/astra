package io.astra.api;

import java.util.*;

/** Definition of a compound task with its decompositions. */
public class CompoundTaskDef {
    private final String name;
    private final String description;
    private final List<DecompositionDef> decompositions;

    public CompoundTaskDef(String name, List<DecompositionDef> decompositions) {
        this(name, "", decompositions);
    }

    public CompoundTaskDef(String name, String description, List<DecompositionDef> decompositions) {
        this.name = name;
        this.description = description;
        this.decompositions = decompositions;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<DecompositionDef> getDecompositions() { return decompositions; }
}
