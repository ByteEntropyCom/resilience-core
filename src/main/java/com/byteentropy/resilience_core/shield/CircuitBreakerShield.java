package com.byteentropy.resilience_core.shield;

import com.byteentropy.resilience_core.model.PaymentRequest;
import com.byteentropy.resilience_core.model.PaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Component
@Order(20)
public class CircuitBreakerShield implements Shield {
    @Override
    @CircuitBreaker(name = "bankCircuitBreaker")
    public CompletableFuture<PaymentResponse> apply(PaymentRequest request, Supplier<CompletableFuture<PaymentResponse>> next) {
        return next.get();
    }
}