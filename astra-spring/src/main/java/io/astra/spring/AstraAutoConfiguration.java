package io.astra.spring;

import io.astra.api.Astra;
import io.astra.annotation.Agent;
import io.astra.core.DefaultAstra;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AstraAutoConfiguration {

    @Bean
    public Astra astra(ApplicationContext context) {
        DefaultAstra.Builder builder = DefaultAstra.builder();
        var agents = context.getBeansWithAnnotation(Agent.class);
        for (Object agent : agents.values()) {
            builder.register(agent);
        }
        return builder.build();
    }
}
