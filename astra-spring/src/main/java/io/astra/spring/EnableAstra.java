package io.astra.spring;

import org.springframework.context.annotation.Import;
import java.lang.annotation.*;

/** Enables Astra auto-configuration in a Spring application. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(AstraAutoConfiguration.class)
public @interface EnableAstra {
}
