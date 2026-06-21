package io.astra.annotation.fact;

import java.lang.annotation.*;

/** Container for repeatable {@link Fact} annotations. */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Facts {
    Fact[] value();
}
