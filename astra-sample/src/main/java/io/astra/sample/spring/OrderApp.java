package io.astra.sample.spring;

import io.astra.api.Astra;
import io.astra.api.WorldStates;
import io.astra.api.result.ExecutionResult;
import io.astra.spring.EnableAstra;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

@SpringBootApplication
@EnableAstra
public class OrderApp {
    public static void main(String[] args) {
        SpringApplication.run(OrderApp.class, args);
    }
}

@Service
class OrderService {
    private final Astra astra;

    OrderService(Astra astra) {
        this.astra = astra;
    }

    ExecutionResult process(String orderId) {
        return astra.executeWithResult("ProcessOrder",
            WorldStates.of("orderReceived", "true"));
    }
}
