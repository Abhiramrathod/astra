package io.astra.annotation;

import java.lang.annotation.*;

/** Specifies a policy required to invoke the annotated method. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresPolicy {
    String value();
}
