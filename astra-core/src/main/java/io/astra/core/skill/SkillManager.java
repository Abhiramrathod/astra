package io.astra.core.skill;

import io.astra.api.config.AstraConfig;
import io.astra.api.skill.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the lifecycle (load, unload, query) of {@link Skill} plugins
 * keyed by name.
 */
public class SkillManager {
    private static final Logger log = LoggerFactory.getLogger(SkillManager.class);
    private final Map<String, Skill> skills = new ConcurrentHashMap<>();
    private final AstraConfig config;

    public SkillManager(AstraConfig config) {
        this.config = config;
    }

    public void load(Skill skill) {
        String name = skill.getName();
        if (skills.containsKey(name)) {
            log.warn("Skill '{}' already loaded, unloading first", name);
            unload(name);
        }
        skill.init(config);
        skills.put(name, skill);
        log.info("Skill '{}' v{} loaded", name, skill.getVersion());
    }

    public void unload(String name) {
        Skill skill = skills.remove(name);
        if (skill != null) {
            skill.destroy();
            log.info("Skill '{}' unloaded", name);
        }
    }

    public Optional<Skill> getSkill(String name) {
        return Optional.ofNullable(skills.get(name));
    }

    public List<String> getSkillNames() {
        return List.copyOf(skills.keySet());
    }

    public void unloadAll() {
        new ArrayList<>(skills.keySet()).forEach(this::unload);
    }
}
