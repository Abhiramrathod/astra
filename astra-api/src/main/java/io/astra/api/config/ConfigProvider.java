package io.astra.api.config;

/**
 * Provider for obtaining an {@link AstraConfig} instance.
 */
public interface ConfigProvider {
    AstraConfig getConfig();
}
