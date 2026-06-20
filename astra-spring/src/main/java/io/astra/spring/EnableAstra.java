package io.astra.spring;

import org.springframework.context.annotation.Import;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(AstraAutoConfiguration.class)
public @interface EnableAstra {
}
