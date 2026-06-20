package io.astra.annotation.decomposition;

import io.astra.annotation.fact.Fact;
import java.lang.annotation.*;

@Repeatable(Decompositions.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Decomposition {
    String name() default "";
    String description() default "";
    Fact[] preconditions() default {};
    String[] subtasks();
}
