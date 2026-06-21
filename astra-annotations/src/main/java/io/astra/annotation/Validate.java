package io.astra.annotation;

import java.lang.annotation.*;

/** Declares validation rules for a method. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Validate {
    String[] rules() default {};
}
