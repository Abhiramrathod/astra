package io.astra.api.skill;

import io.astra.api.config.AstraConfig;

/**
 * Pluggable skill that can be loaded, initialized, and destroyed.
 */
public interface Skill {
    String getName();
    String getVersion();
    void init(AstraConfig config);
    void destroy();
}
