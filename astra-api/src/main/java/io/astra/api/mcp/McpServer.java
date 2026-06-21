package io.astra.api.mcp;

import java.util.List;
import java.util.Map;

/**
 * Server-side MCP interface for tool registration and invocation.
 */
public interface McpServer {
    String getServerInfo();
    List<McpTool> listTools();
    Map<String, Object> callTool(String toolName, Map<String, Object> args);
    void start();
    void stop();
}
