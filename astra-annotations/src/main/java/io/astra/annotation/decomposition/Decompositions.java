package io.astra.annotation.decomposition;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Decompositions {
    Decomposition[] value();
}
