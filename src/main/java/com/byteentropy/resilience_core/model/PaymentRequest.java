package com.byteentropy.resilience_core.model;

import java.math.BigDecimal;

public record PaymentRequest(
        String idempotencyId,
        BigDecimal amount,
        String currency,
        String merchantId,
        String terminalId,
        String paymentMethodType,
        String encryptedCardData,
        String orderReference
) {}