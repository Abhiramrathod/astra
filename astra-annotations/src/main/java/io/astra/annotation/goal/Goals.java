package io.astra.annotation.goal;

import java.lang.annotation.*;

/** Container for repeatable {@link Goal} annotations. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Goals {
    Goal[] value();
}
