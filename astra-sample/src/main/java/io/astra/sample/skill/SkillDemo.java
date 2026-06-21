package io.astra.sample.skill;

import io.astra.api.*;
import io.astra.api.config.AstraConfig;
import io.astra.api.skill.Skill;
import io.astra.core.skill.SkillManager;

/** Demonstrates loading and unloading a {@link Skill} via {@link SkillManager}. */
public class SkillDemo {
    public static void run() {
        System.out.println("\n=== Skill: Skill Lifecycle ===");
        SkillManager mgr = new SkillManager(null);
        Skill logging = new Skill() {
            @Override public String getName() { return "logging"; }
            @Override public String getVersion() { return "1.0"; }
            @Override public void init(AstraConfig config) { System.out.println("  Skill initialized: logging"); }
            @Override public void destroy() { System.out.println("  Skill destroyed: logging"); }
        };
        mgr.load(logging);
        System.out.println("  Loaded skills: " + mgr.getSkillNames());
        mgr.unload("logging");
        System.out.println("  Skill demo complete");
    }
}
