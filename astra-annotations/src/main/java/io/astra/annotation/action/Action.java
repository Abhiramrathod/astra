package io.astra.annotation.action;

import io.astra.annotation.fact.Fact;
import java.lang.annotation.*;

/** Defines an action with preconditions, effects, cost, and utility. */
@Repeatable(Actions.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Action {
    String name() default "";
    String description() default "";
    Fact[] preconditions() default {};
    Fact[] effects() default {};
    float cost() default 1.0f;
    float utility() default 0.5f;
    String utilityMethod() default "";
}
