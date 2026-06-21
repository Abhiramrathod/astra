package io.astra.annotation;

import java.lang.annotation.*;

/** Defines a skill that an agent can perform. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Skill {
    String name();
    String version() default "1.0.0";
}
