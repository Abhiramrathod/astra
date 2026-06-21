package io.astra.core.policy;

import io.astra.api.WorldState;
import io.astra.api.policy.PolicyChecker;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory {@link PolicyChecker} that stores granted policies
 * per action name in a concurrent map.
 */
public class DefaultPolicyChecker implements PolicyChecker {
    private final Map<String, String> policies = new ConcurrentHashMap<>();

    public void grant(String actionName, String policy) {
        policies.put(actionName, policy);
    }

    public void revoke(String actionName) {
        policies.remove(actionName);
    }

    @Override
    public boolean check(String actionName, String policy, WorldState state) {
        String granted = policies.get(actionName);
        return policy.equals(granted) || "admin".equals(granted);
    }
}
