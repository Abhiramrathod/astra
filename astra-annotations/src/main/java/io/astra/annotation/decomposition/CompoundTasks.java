package io.astra.annotation.decomposition;

import java.lang.annotation.*;

/** Container for repeatable {@link CompoundTask} annotations. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CompoundTasks {
    CompoundTask[] value();
}
