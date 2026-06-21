package io.astra.api.mcp;

import java.util.List;
import java.util.Map;

/**
 * Client for connecting to MCP servers and invoking tools.
 */
public interface McpClient {
    void connect(String serverUrl);
    void disconnect();
    List<McpTool> listTools();
    Map<String, Object> callTool(String toolName, Map<String, Object> args);
    boolean isConnected();
}
