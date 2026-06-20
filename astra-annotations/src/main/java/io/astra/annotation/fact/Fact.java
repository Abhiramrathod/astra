package io.astra.annotation.fact;

import java.lang.annotation.*;

@Repeatable(Facts.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Fact {
    String name();
    String value();
}
