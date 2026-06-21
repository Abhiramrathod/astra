package io.astra.annotation;

import java.lang.annotation.*;

/** Marks a class as an Astra agent. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Agent {
    String name() default "";
}
