package io.astra.annotation.goal;

import io.astra.annotation.fact.Fact;
import java.lang.annotation.*;

/** Represents a goal with a name, description, and success condition. */
@Repeatable(Goals.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Goal {
    String name() default "";
    String description() default "";
    Fact[] condition() default {};
}
