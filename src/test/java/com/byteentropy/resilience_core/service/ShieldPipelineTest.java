package com.byteentropy.resilience_core.service;

import com.byteentropy.resilience_core.client.ExternalBankClient;
import com.byteentropy.resilience_core.model.PaymentRequest;
import com.byteentropy.resilience_core.model.PaymentResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ShieldPipelineTest {

    @Autowired
    private ShieldPipeline pipeline;

    @Autowired
    private CircuitBreakerRegistry registry;

    @SpyBean
    private ExternalBankClient bankClient;

    private PaymentRequest request;

    @BeforeEach
    void setUp() {
        request = new PaymentRequest(
                UUID.randomUUID().toString(),
                new BigDecimal("100.00"),
                "USD",
                "MERCH-1",
                "TERM-1",
                "CARD",
                "secret",
                "REF-1"
        );
    }

    @Test
    void testSuccessfulPayment() throws Exception {
        bankClient.setMode(ExternalBankClient.Mode.SUCCESS);
        PaymentResponse response = pipeline.execute(request).get(5, TimeUnit.SECONDS);
        assertThat(response.status()).isEqualTo("AUTHORIZED");
    }

    @Test
    void testRetryExhaustion() throws Exception {
        bankClient.setMode(ExternalBankClient.Mode.FAILURE);
        PaymentResponse response = pipeline.execute(request).get(10, TimeUnit.SECONDS);
        
        // UPDATED: Now 1 time because we don't retry generic RuntimeExceptions anymore
        verify(bankClient, times(1)).call(any()); 
        assertThat(response.status()).isEqualTo("FAILED");
    }

    @Test
    void testTimeLimiterTrigger() throws Exception {
        bankClient.setMode(ExternalBankClient.Mode.SLOW);
        PaymentResponse response = pipeline.execute(request).get(10, TimeUnit.SECONDS);
        
        // UPDATED: Asserting the new fintech-safe status
        assertThat(response.status()).isEqualTo("UNCERTAIN");
        assertThat(response.message()).contains("timed out");
    }

    @Test
    void testTimeoutMode() throws Exception {
        bankClient.setMode(ExternalBankClient.Mode.TIMEOUT);
        PaymentResponse response = pipeline.execute(request).get(10, TimeUnit.SECONDS);
        
        // UPDATED: Asserting the new fintech-safe status
        assertThat(response.status()).isEqualTo("UNCERTAIN");
        assertThat(response.message()).contains("timed out");
    }

    @Test
    void testRateLimiterRejection() throws Exception {
        bankClient.setMode(ExternalBankClient.Mode.SUCCESS);
        int burstSize = 25;
        List<CompletableFuture<PaymentResponse>> futures = new ArrayList<>();

        for (int i = 0; i < burstSize; i++) {
            futures.add(pipeline.execute(request)); 
        }

        List<PaymentResponse> responses = futures.stream()
                .map(f -> {
                    try { return f.get(5, TimeUnit.SECONDS); } 
                    catch (Exception e) { return null; }
                })
                .filter(Objects::nonNull)
                .toList();

        long rejectedCount = responses.stream()
                .filter(r -> "REJECTED".equals(r.status()))
                .count();

        assertThat(rejectedCount).isGreaterThan(0);
    }

    @Test
    void testCircuitBreakerTrips() throws Exception {
        bankClient.setMode(ExternalBankClient.Mode.FAILURE);
        CircuitBreaker cb = registry.circuitBreaker("bankCircuitBreaker");

        for (int i = 0; i < 15; i++) {
            pipeline.execute(request).handle((res, ex) -> null).get(5, TimeUnit.SECONDS);
        }

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN));

        PaymentResponse response = pipeline.execute(request).get(5, TimeUnit.SECONDS);
        assertThat(response.message()).contains("circuit open");
    }

    @Test
    void testCircuitBreakerRecovery() throws Exception {
        bankClient.setMode(ExternalBankClient.Mode.FAILURE);
        CircuitBreaker cb = registry.circuitBreaker("bankCircuitBreaker");

        for (int i = 0; i < 15; i++) {
            pipeline.execute(request).handle((res, ex) -> null).get(5, TimeUnit.SECONDS);
        }

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN));

        // Wait for CB to transition to half-open/closed
        Thread.sleep(6000); 

        bankClient.setMode(ExternalBankClient.Mode.SUCCESS);
        PaymentResponse response = pipeline.execute(request).get(5, TimeUnit.SECONDS);
        assertThat(response.status()).isEqualTo("AUTHORIZED");
    }
}
