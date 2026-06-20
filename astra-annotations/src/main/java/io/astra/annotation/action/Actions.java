package io.astra.annotation.action;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Actions {
    Action[] value();
}
