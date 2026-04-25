package com.byteentropy.resilience_core.shield;

import com.byteentropy.resilience_core.model.PaymentRequest;
import com.byteentropy.resilience_core.model.PaymentResponse;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Component
@Order(40)
public class TimeLimiterShield implements Shield {
    @Override
    @TimeLimiter(name = "bankTimeout")
    public CompletableFuture<PaymentResponse> apply(PaymentRequest request, Supplier<CompletableFuture<PaymentResponse>> next) {
        return next.get();
    }
}