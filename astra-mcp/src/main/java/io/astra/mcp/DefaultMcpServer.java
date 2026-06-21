package io.astra.mcp;

import io.astra.api.ActionInfo;
import io.astra.api.mcp.McpTool;
import io.astra.api.mcp.McpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link McpServer} that manages registered tools locally.
 */
public class DefaultMcpServer implements McpServer {
    private static final Logger log = LoggerFactory.getLogger(DefaultMcpServer.class);
    private final String name;
    private final Map<String, McpTool> tools = new ConcurrentHashMap<>();
    private boolean running;

    public DefaultMcpServer(String name) {
        this.name = name;
    }

    public void registerTool(McpTool tool) {
        tools.put(tool.getName(), tool);
        log.info("MCP tool registered: {}", tool.getName());
    }

    public void registerActionAsTool(ActionInfo action) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        for (var entry : action.getPreconditions().entrySet()) {
            props.put(entry.getKey(), Map.of("type", "string", "description", entry.getValue()));
        }
        schema.put("properties", props);
        registerTool(new McpTool(action.getName(), action.getDescription(), schema));
    }

    @Override
    public String getServerInfo() { return "MCP:" + name; }

    @Override
    public List<McpTool> listTools() { return List.copyOf(tools.values()); }

    @Override
    public Map<String, Object> callTool(String toolName, Map<String, Object> args) {
        McpTool tool = tools.get(toolName);
        if (tool == null) throw new IllegalArgumentException("Unknown tool: " + toolName);
        log.info("MCP tool call: {} with args {}", toolName, args);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "called");
        result.put("tool", toolName);
        result.put("args", args);
        return result;
    }

    @Override
    public void start() {
        running = true;
        log.info("MCP server '{}' started with {} tools", name, tools.size());
    }

    @Override
    public void stop() {
        running = false;
        log.info("MCP server '{}' stopped", name);
    }
}
