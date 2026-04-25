package com.byteentropy.resilience_core.service;

import com.byteentropy.resilience_core.client.ExternalBankClient;
import com.byteentropy.resilience_core.model.PaymentRequest;
import com.byteentropy.resilience_core.model.PaymentResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Service
public class ShieldPipeline {

    private static final Logger log = LoggerFactory.getLogger(ShieldPipeline.class);

    private final ExternalBankClient bankClient;
    private final Executor executor;

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final RateLimiter rateLimiter;
    private final TimeLimiter timeLimiter;

    public ShieldPipeline(ExternalBankClient bankClient,
                          @Qualifier("virtualThreadExecutor") Executor executor,
                          CircuitBreakerRegistry circuitBreakerRegistry,
                          RetryRegistry retryRegistry,
                          RateLimiterRegistry rateLimiterRegistry,
                          TimeLimiterRegistry timeLimiterRegistry) {

        this.bankClient = bankClient;
        this.executor = executor;

        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("bankCircuitBreaker");
        this.retry = retryRegistry.retry("bankRetry");
        this.rateLimiter = rateLimiterRegistry.rateLimiter("bankRateLimit");
        this.timeLimiter = timeLimiterRegistry.timeLimiter("bankTimeout");
    }

    /**
     * Executes payment with resilience patterns in this order:
     * RateLimiter (outer) → Retry → CircuitBreaker → TimeLimiter (inner)
     */
    public CompletableFuture<PaymentResponse> execute(PaymentRequest request) {
        Supplier<PaymentResponse> rawSupplier = () -> bankClient.call(request);

        // RateLimiter outermost → prevents too many requests from hitting Retry/CB
        Supplier<PaymentResponse> decorated = RateLimiter.decorateSupplier(rateLimiter, rawSupplier);

        // Then Retry
        decorated = Retry.decorateSupplier(retry, decorated);

        // Then CircuitBreaker
        decorated = CircuitBreaker.decorateSupplier(circuitBreaker, decorated);

        // Execute on virtual thread
        CompletableFuture<PaymentResponse> callFuture = CompletableFuture.supplyAsync(decorated, executor);

        // Apply TimeLimiter + fallback
        return CompletableFuture.supplyAsync(() -> {
            try {
                return timeLimiter.executeFutureSupplier(() -> callFuture);
            } catch (Throwable ex) {
                return handleFallback(request, ex);
            }
        }, executor);
    }

    private PaymentResponse handleFallback(PaymentRequest req, Throwable t) {
        Throwable actual = (t instanceof CompletionException ce) ? ce.getCause() : t;
        if (actual == null) actual = t;

        log.error("Fallback triggered for IdempotencyId: {}. Cause: {}", 
                  req.idempotencyId(), actual.getClass().getSimpleName(), actual);

        String status = "FAILED";
        String msg = actual.getMessage() != null ? actual.getMessage() : "Unknown error";

        if (actual instanceof io.github.resilience4j.ratelimiter.RequestNotPermitted) {
            status = "REJECTED";
            msg = "Rate limit exceeded (Max 10 requests/sec)";
        } else if (actual instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
            msg = "Service unavailable (circuit open)";
        } else if (isTimeout(actual)) {
            msg = "Request timed out (Bank took too long)";
        }

        return new PaymentResponse(req.idempotencyId(), null, status, null, msg);
    }

    private boolean isTimeout(Throwable t) {
        if (t == null) return false;
        String msg = t.getMessage() != null ? t.getMessage().toLowerCase() : "";
        return t instanceof java.util.concurrent.TimeoutException ||
               (t.getCause() != null && t.getCause() instanceof java.util.concurrent.TimeoutException) ||
               msg.contains("timeout") || msg.contains("timed out");
    }
}