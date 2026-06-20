package io.astra.annotation.fact;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Facts {
    Fact[] value();
}
