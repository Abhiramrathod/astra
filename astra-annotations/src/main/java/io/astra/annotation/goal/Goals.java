package io.astra.annotation.goal;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Goals {
    Goal[] value();
}
