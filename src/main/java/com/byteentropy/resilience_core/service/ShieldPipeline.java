package com.byteentropy.resilience_core.service;

import com.byteentropy.resilience_core.client.ExternalBankClient;
import com.byteentropy.resilience_core.model.*;
import com.byteentropy.resilience_core.repository.PaymentRepository;
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
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Service
public class ShieldPipeline {
    private static final Logger log = LoggerFactory.getLogger(ShieldPipeline.class);

    private final ExternalBankClient bankClient;
    private final PaymentRepository repository;
    private final Executor executor;

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final RateLimiter rateLimiter;
    private final TimeLimiter timeLimiter;

    public ShieldPipeline(ExternalBankClient bankClient,
                          PaymentRepository repository,
                          @Qualifier("virtualThreadExecutor") Executor executor,
                          CircuitBreakerRegistry cbRegistry,
                          RetryRegistry retryRegistry,
                          RateLimiterRegistry rlRegistry,
                          TimeLimiterRegistry tlRegistry) {
        this.bankClient = bankClient;
        this.repository = repository;
        this.executor = executor;
        this.circuitBreaker = cbRegistry.circuitBreaker("bankCircuitBreaker");
        this.retry = retryRegistry.retry("bankRetry");
        this.rateLimiter = rlRegistry.rateLimiter("bankRateLimit");
        this.timeLimiter = tlRegistry.timeLimiter("bankTimeout");
    }

    public CompletableFuture<PaymentResponse> execute(PaymentRequest request) {
        // Idempotency: Check DB first
        return repository.findById(request.idempotencyId())
            .map(e -> {
                log.info("[IDEMPOTENCY] Cache Hit: {}", request.idempotencyId());
                return CompletableFuture.completedFuture(new PaymentResponse(
                    e.getIdempotencyId(), e.getGatewayTxnId(), e.getStatus(), null, e.getMessage()));
            })
            .orElseGet(() -> processRequest(request));
    }

    private CompletableFuture<PaymentResponse> processRequest(PaymentRequest request) {
        Supplier<PaymentResponse> bankCall = () -> bankClient.call(request);

        // Chain: RateLimit -> Retry -> CircuitBreaker
        Supplier<PaymentResponse> resilientCall = RateLimiter.decorateSupplier(rateLimiter, 
            Retry.decorateSupplier(retry, 
                CircuitBreaker.decorateSupplier(circuitBreaker, bankCall)
            )
        );

        return CompletableFuture.supplyAsync(() -> {
            try {
                PaymentResponse response = timeLimiter.executeFutureSupplier(() -> 
                    CompletableFuture.supplyAsync(resilientCall, executor)
                );
                repository.save(new PaymentEntity(response.idempotencyId(), response.status(), response.gatewayTxnId(), response.message()));
                return response;
            } catch (Throwable t) {
                return handleFallback(request, t);
            }
        }, executor);
    }

    private PaymentResponse handleFallback(PaymentRequest req, Throwable t) {
        String status = "FAILED";
        String msg = (t.getCause() != null) ? t.getCause().getMessage() : t.getMessage();

        if (t instanceof io.github.resilience4j.ratelimiter.RequestNotPermitted) {
            status = "REJECTED";
            msg = "Rate limit exceeded";
        } else if (t.getCause() instanceof java.util.concurrent.TimeoutException) {
            status = "UNCERTAIN";
            msg = "Bank Timeout - Verification required";
            repository.save(new PaymentEntity(req.idempotencyId(), status, null, msg));
        }

        return new PaymentResponse(req.idempotencyId(), null, status, null, msg);
    }
}
