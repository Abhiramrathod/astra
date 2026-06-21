package io.astra.core.policy;

import io.astra.api.ActionInfo;
import io.astra.api.WorldState;
import io.astra.api.interceptor.ActionInterceptor;
import io.astra.api.policy.PolicyChecker;
import io.astra.api.event.AstraEvent;
import io.astra.api.event.AstraEventType;
import io.astra.api.event.EventBus;

/**
 * {@link ActionInterceptor} that checks a {@link PolicyChecker} before
 * action execution and throws {@link SecurityException} on denial.
 */
public class PolicyInterceptor implements ActionInterceptor {
    private final PolicyChecker checker;
    private final EventBus eventBus;

    public PolicyInterceptor(PolicyChecker checker, EventBus eventBus) {
        this.checker = checker;
        this.eventBus = eventBus;
    }

    @Override
    public void beforeAction(ActionInfo action, WorldState state) {
        String policyName = action.getName() + ".policy";
        String expectedPolicy = System.getProperty(policyName, "default");
        if (!checker.check(action.getName(), expectedPolicy, state)) {
            eventBus.publish(AstraEvent.of(AstraEventType.POLICY_DENIED,
                "action", action.getName(), "policy", expectedPolicy));
            throw new SecurityException("Policy denied: " + expectedPolicy + " for action " + action.getName());
        }
    }
}
