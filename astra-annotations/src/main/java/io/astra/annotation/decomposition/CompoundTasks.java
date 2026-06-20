package io.astra.annotation.decomposition;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CompoundTasks {
    CompoundTask[] value();
}
