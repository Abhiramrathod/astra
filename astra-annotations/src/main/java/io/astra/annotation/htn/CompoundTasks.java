package io.astra.annotation.htn;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CompoundTasks {
    CompoundTask[] value();
}
