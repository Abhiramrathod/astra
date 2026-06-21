package io.astra.annotation.fact;

import java.lang.annotation.*;

/** A key-value fact representing a piece of world state. */
@Repeatable(Facts.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Fact {
    String name();
    String value();
}
