package com.byteentropy.resilience_core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ResilienceCoreApplicationTests {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Test
    void contextLoads() {
        // Ensures Spring starts
    }

    @Test
    void resilienceBeansAreLoaded() {
        assertThat(circuitBreakerRegistry.circuitBreaker("bankCircuitBreaker")).isNotNull();
    }
}
