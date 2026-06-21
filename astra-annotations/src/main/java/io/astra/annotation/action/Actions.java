package io.astra.annotation.action;

import java.lang.annotation.*;

/** Container for repeatable {@link Action} annotations. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Actions {
    Action[] value();
}
