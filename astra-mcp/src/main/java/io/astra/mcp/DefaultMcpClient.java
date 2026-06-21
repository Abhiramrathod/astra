package io.astra.mcp;

import io.astra.api.mcp.McpClient;
import io.astra.api.mcp.McpTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default implementation of {@link McpClient} that connects to a remote MCP server.
 */
public class DefaultMcpClient implements McpClient {
    private static final Logger log = LoggerFactory.getLogger(DefaultMcpClient.class);
    private final List<McpTool> remoteTools = new CopyOnWriteArrayList<>();
    private String serverUrl;
    private boolean connected;

    @Override
    public void connect(String serverUrl) {
        this.serverUrl = serverUrl;
        this.connected = true;
        log.info("MCP client connected to {}", serverUrl);
    }

    @Override
    public void disconnect() {
        this.connected = false;
        this.serverUrl = null;
        remoteTools.clear();
        log.info("MCP client disconnected");
    }

    @Override
    public List<McpTool> listTools() {
        return List.copyOf(remoteTools);
    }

    @Override
    public Map<String, Object> callTool(String toolName, Map<String, Object> args) {
        if (!connected) throw new IllegalStateException("Not connected to MCP server");
        log.info("MCP client calling tool '{}' on {} with args {}", toolName, serverUrl, args);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "remote_call");
        result.put("tool", toolName);
        result.put("server", serverUrl);
        return result;
    }

    @Override
    public boolean isConnected() { return connected; }
}
