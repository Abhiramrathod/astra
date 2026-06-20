package io.astra.annotation.decomposition;

import java.lang.annotation.*;

@Repeatable(CompoundTasks.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CompoundTask {
    String name() default "";
    String description() default "";
}
