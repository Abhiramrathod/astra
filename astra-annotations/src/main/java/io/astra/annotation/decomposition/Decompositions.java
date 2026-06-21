package io.astra.annotation.decomposition;

import java.lang.annotation.*;

/** Container for repeatable {@link Decomposition} annotations. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Decompositions {
    Decomposition[] value();
}
