package com.byteentropy.resilience_core.shield;

import com.byteentropy.resilience_core.model.PaymentRequest;
import com.byteentropy.resilience_core.model.PaymentResponse;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

@Component
@Order(10)
public class RetryShield implements Shield {
    private static final Logger log = LoggerFactory.getLogger(RetryShield.class);

    @Override
    @Retry(name = "bankRetry", fallbackMethod = "fallback")
    public CompletableFuture<PaymentResponse> apply(PaymentRequest request, Supplier<CompletableFuture<PaymentResponse>> next) {
        return next.get();
    }

    /**
     * The Global Fallback for the Resilience Pipeline.
     * This method catches exceptions from RateLimiter, CircuitBreaker, and TimeLimiter.
     */
public CompletableFuture<PaymentResponse> fallback(
        PaymentRequest req, 
        Supplier<CompletableFuture<PaymentResponse>> next, 
        Throwable t) {

    Throwable actualCause = (t instanceof CompletionException ce) ? ce.getCause() : t;
    if (actualCause == null) actualCause = t;

    log.error("Resilience Shield fallback for ID: {}. Cause: {} | Msg: {}", 
              req.idempotencyId(), actualCause.getClass().getSimpleName(), actualCause.getMessage());

    String status = "FAILED";
    String msg = actualCause.getMessage() != null ? actualCause.getMessage() : "Unknown error";

    if (actualCause instanceof io.github.resilience4j.ratelimiter.RequestNotPermitted) {
        status = "REJECTED";
        msg = "Rate limit exceeded (Max 10 requests/sec)";
    } else if (actualCause instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
        status = "FAILED";
        msg = "Service unavailable (circuit open)";
    } else if (actualCause instanceof java.util.concurrent.TimeoutException 
            || actualCause.getCause() instanceof java.util.concurrent.TimeoutException
            || msg.contains("timeout") || msg.contains("timed out")) {
        status = "FAILED";
        msg = "Request timed out (Bank took too long)";
    }

    return CompletableFuture.completedFuture(
            new PaymentResponse(req.idempotencyId(), null, status, null, msg)
    );
}
}