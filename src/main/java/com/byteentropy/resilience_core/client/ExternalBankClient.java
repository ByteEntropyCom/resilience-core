package com.byteentropy.resilience_core.client;

import com.byteentropy.resilience_core.model.PaymentRequest;
import com.byteentropy.resilience_core.model.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class ExternalBankClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalBankClient.class);

    public enum Mode { SUCCESS, FAILURE, TIMEOUT, SLOW }

    private Mode mode = Mode.SUCCESS;

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public PaymentResponse call(PaymentRequest request) {
        log.info("[BANK CALL] Mode: {} | IdempotencyId: {}", mode, request.idempotencyId());

        return switch (mode) {
            case SUCCESS -> new PaymentResponse(
                    request.idempotencyId(),
                    "TXN-" + UUID.randomUUID(),
                    "AUTHORIZED",
                    "AUTH-123",
                    "Success"
            );
            case FAILURE -> {
                log.error("[BANK ERROR] Simulating 500 for ID: {}", request.idempotencyId());
                throw new RuntimeException("Bank Backend 500 Internal Error");
            }
            case TIMEOUT -> {
                log.error("[BANK TIMEOUT] Simulating SocketTimeout for ID: {}", request.idempotencyId());
                throw new RuntimeException(new java.net.SocketTimeoutException("Read timed out"));
            }
            case SLOW -> {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    log.warn("[BANK INTERRUPT] Thread interrupted for ID: {}", request.idempotencyId());
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Request interrupted", e);
                }
                yield new PaymentResponse(
                        request.idempotencyId(),
                        "TXN-SLOW",
                        "AUTHORIZED",
                        "AUTH-SLOW",
                        "Slow success"
                );
            }
        };
    }
}