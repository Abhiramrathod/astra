package io.astra.api.composite;

import io.astra.api.*;
import io.astra.api.result.ExecutionResult;
import java.util.List;

/**
 * An agent composed of multiple sub-agents with delegated execution.
 */
public interface CompositeAgent extends Astra {
    void registerSubAgent(String name, Astra agent);
    void removeSubAgent(String name);
    List<String> getSubAgentNames();
    ExecutionResult executeQuery(String query, String preferredAgent);
}
